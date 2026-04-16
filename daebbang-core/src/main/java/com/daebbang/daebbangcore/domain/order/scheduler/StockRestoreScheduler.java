package com.daebbang.daebbangcore.domain.order.scheduler;

import com.daebbang.daebbangcore.domain.order.repository.OrdersRepository;
import com.daebbang.daebbangcore.domain.order.service.StockCacheService;
import com.daebbang.daebbangcore.domain.order.session.OrderStockReserveRedisRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreScheduler {

    private static final String RESERVE_PREFIX = "order:stock-reserve:";
    private static final String SESSION_PREFIX = "order:session:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderStockReserveRedisRepository stockReserveRepository;
    private final StockCacheService stockCacheService;
    private final OrdersRepository ordersRepository;

    @Scheduled(fixedDelay = 300_000)
    public void restoreExpiredStocks() {
        Set<String> keys = redisTemplate.keys(RESERVE_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        log.info("[StockRestore] 스케줄러 실행 - 대상 키 수: {}", keys.size());

        for (String key : keys) {
            String orderNumber = key.substring(RESERVE_PREFIX.length());

            // 세션이 살아있으면 결제 진행중 → 스킵
            if (Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX + orderNumber))) continue;

            // DB에 주문이 있으면 결제 완료 → 예약 키만 삭제
            if (ordersRepository.existsByOrderNumber(orderNumber)) {
                stockReserveRepository.delete(orderNumber);
                log.info("[StockRestore] 결제 완료된 예약 키 삭제 - orderNumber: {}", orderNumber);
                continue;
            }

            // 세션 없음 + 미결제 → 재고 복구
            stockReserveRepository.findByOrderNumber(orderNumber).ifPresent(items -> {
                items.forEach(item ->
                    stockCacheService.restoreStock(item.getProductDetailId(), item.getQuantity())
                );
                stockReserveRepository.delete(orderNumber);
                log.info("[StockRestore] 스케줄러로 재고 복구 완료 - orderNumber: {}", orderNumber);
            });
        }
    }
}
