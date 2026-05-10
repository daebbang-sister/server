package com.daebbang.daebbangcore.domain.order.scheduler;

import com.daebbang.daebbangcore.domain.order.service.OrderService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCompleteScheduler {

    private static final String SCHEDULER_LOCK_KEY = "scheduler:order-auto-complete:lock";

    private final OrderService orderService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 5 * * *")
    public void autoCompleteDeliveredOrders() {
        RLock lock = redissonClient.getLock(SCHEDULER_LOCK_KEY);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, 600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.info("[OrderAutoComplete] 다른 인스턴스가 실행 중 - 스킵");
            return;
        }

        try {
            List<Long> targets = orderService.findAutoCompletableOrderIds();
            if (targets.isEmpty()) {
                return;
            }

            log.info("[OrderAutoComplete] 자동 확정 대상 - {}건", targets.size());

            int success = 0;
            int failure = 0;
            for (Long orderId : targets) {
                try {
                    orderService.autoComplete(orderId);
                    success++;
                } catch (Exception e) {
                    failure++;
                    log.error("[OrderAutoComplete] 단건 자동 확정 실패 - orderId: {}", orderId, e);
                }
            }
            log.info("[OrderAutoComplete] 완료 - 성공: {}, 실패: {}", success, failure);
        } finally {
            lock.unlock();
        }
    }
}
