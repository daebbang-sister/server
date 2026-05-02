package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import com.daebbang.daebbangcore.domain.order.event.StockInvalidateEvent;
import com.daebbang.daebbangcore.domain.product.service.ProductDetailsService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockCacheService {

    private static final String STOCK_KEY_PREFIX = "stock:";
    private static final Duration STOCK_TTL = Duration.ofHours(1);

    private final StringRedisTemplate stringRedisTemplate;
    private final ProductDetailsService productDetailsService;

    public void decreaseStock(Long productDetailId, int quantity) {
        String key = STOCK_KEY_PREFIX + productDetailId;

        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(key))) {
            int dbStock = productDetailsService.getProductDetailsById(productDetailId).getStock();

            Boolean initialized = stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(dbStock), STOCK_TTL);
            if (Boolean.TRUE.equals(initialized)) {
                log.info("[Stock] 캐시 적재 - productDetailId: {}, stock: {}", productDetailId, dbStock);
            }
        }

        Long result = stringRedisTemplate.opsForValue().decrement(key, quantity);
        if (result == null || result < 0) {
            stringRedisTemplate.opsForValue().increment(key, quantity);
            log.warn("[Stock] 재고 부족 - productDetailId: {}, 요청수량: {}", productDetailId, quantity);
            throw new BusinessException(OrderErrorCode.OUT_OF_STOCK);
        }

        log.info("[Stock] 감소 완료 - productDetailId: {}, 차감: {}, 잔여: {}", productDetailId, quantity, result);
    }

    public void restoreStock(Long productDetailId, int quantity) {
        String key = STOCK_KEY_PREFIX + productDetailId;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().increment(key, quantity);
            log.info("[Stock] 복원 - productDetailId: {}, 복원수량: {}", productDetailId, quantity);
        } else {
            log.warn("[Stock] 복원 스킵 - 캐시 없음 productDetailId: {}, 수량: {} (캐시 만료 또는 무효화)", productDetailId, quantity);
        }
    }

    public void invalidateStock(Long productDetailId) {
        stringRedisTemplate.delete(STOCK_KEY_PREFIX + productDetailId);
        log.info("[Stock] 캐시 무효화 - productDetailId: {}", productDetailId);
    }

    /**
     * 주문 취소 트랜잭션 커밋 완료 후 Redis 캐시 비동기 무효화.
     * AFTER_COMMIT + @Async → 커밋 확정 후 별도 스레드에서 실행.
     * 트랜잭션 롤백 시에는 실행되지 않아 DB/Redis 정합성 보장.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockInvalidate(StockInvalidateEvent event) {
        event.productDetailIds().forEach(id -> {
            try {
                invalidateStock(id);
            } catch (Exception e) {
                log.error("[Stock] 취소 후 캐시 무효화 실패 - productDetailId: {} (다음 요청 시 DB 재로드)", id, e);
            }
        });
    }
}
