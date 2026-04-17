package com.daebbang.daebbangcore.domain.order.scheduler;

import com.daebbang.daebbangcore.domain.order.repository.OrdersRepository;
import com.daebbang.daebbangcore.domain.order.service.StockCacheService;
import com.daebbang.daebbangcore.domain.order.session.OrderStockReserveRedisRepository;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreScheduler {

    private static final String RESERVE_PREFIX = "order:stock-reserve:";
    private static final String SESSION_PREFIX = "order:session:";
    private static final String SCHEDULER_LOCK_KEY = "scheduler:stock-restore:lock";

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderStockReserveRedisRepository stockReserveRepository;
    private final StockCacheService stockCacheService;
    private final OrdersRepository ordersRepository;
    private final RedissonClient redissonClient;

    @Scheduled(fixedDelay = 300_000)
    public void restoreExpiredStocks() {
        RLock lock = redissonClient.getLock(SCHEDULER_LOCK_KEY);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.info("[StockRestore] 다른 인스턴스가 실행 중 - 스케줄러 스킵");
            return;
        }

        try {
            List<String> keys = scanReserveKeys();
            if (keys.isEmpty()) return;

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
                    stockReserveRepository.delete(orderNumber);
                    items.forEach(item -> {
                        try {
                            stockCacheService.restoreStock(item.getProductDetailId(), item.getQuantity());
                        } catch (Exception e) {
                            log.error("[StockRestore] 재고 복원 실패 - productDetailId: {}",
                                item.getProductDetailId(), e);
                        }
                    });
                    log.info("[StockRestore] 스케줄러로 재고 복구 완료 - orderNumber: {}", orderNumber);
                });
            }
        } finally {
            lock.unlock();
        }
    }

    private List<String> scanReserveKeys() {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
            .match(RESERVE_PREFIX + "*")
            .count(100)
            .build();

        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                cursor.forEachRemaining(key ->
                    keys.add(new String(key, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                log.error("[StockRestore] SCAN 실패", e);
            }
            return null;
        });

        return keys;
    }
}
