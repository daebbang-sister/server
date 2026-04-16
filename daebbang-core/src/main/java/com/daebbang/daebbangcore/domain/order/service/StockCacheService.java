package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.product.service.ProductDetailsService;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
            stringRedisTemplate.opsForValue().set(key, String.valueOf(dbStock), STOCK_TTL);
            log.info("[Stock] 캐시 적재 - productDetailId: {}, stock: {}", productDetailId, dbStock);
        }

        Long result = stringRedisTemplate.opsForValue().decrement(key, quantity);
        if (result == null || result < 0) {
            stringRedisTemplate.opsForValue().increment(key, quantity);
            log.warn("[Stock] 재고 부족 - productDetailId: {}, 요청수량: {}", productDetailId, quantity);
            throw new BusinessException(UserErrorCode.OUT_OF_STOCK);
        }

        log.info("[Stock] 감소 완료 - productDetailId: {}, 차감: {}, 잔여: {}", productDetailId, quantity, result);
    }

    public void restoreStock(Long productDetailId, int quantity) {
        String key = STOCK_KEY_PREFIX + productDetailId;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            stringRedisTemplate.opsForValue().increment(key, quantity);
            log.info("[Stock] 복원 - productDetailId: {}, 복원수량: {}", productDetailId, quantity);
        }
    }

    /** 결제 확정 후 DB를 신뢰 원본으로 삼아 캐시 무효화 */
    public void invalidateStock(Long productDetailId) {
        stringRedisTemplate.delete(STOCK_KEY_PREFIX + productDetailId);
        log.info("[Stock] 캐시 무효화 - productDetailId: {}", productDetailId);
    }
}
