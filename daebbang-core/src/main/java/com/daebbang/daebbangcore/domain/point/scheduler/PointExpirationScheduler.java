package com.daebbang.daebbangcore.domain.point.scheduler;

import com.daebbang.daebbangcore.domain.point.service.PointService;
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
public class PointExpirationScheduler {

    private static final String SCHEDULER_LOCK_KEY = "scheduler:point-expiration:lock";

    private final PointService pointService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 4 * * *")
    public void expireMaturedPoints() {
        RLock lock = redissonClient.getLock(SCHEDULER_LOCK_KEY);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, 600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.info("[PointExpiration] 다른 인스턴스가 실행 중 - 스킵");
            return;
        }

        try {
            List<Long> targets = pointService.findUserIdsWithExpirablePoints();
            if (targets.isEmpty()) {
                return;
            }

            log.info("[PointExpiration] 만료 후보 - {}건", targets.size());

            int success = 0;
            int failure = 0;
            for (Long pointsId : targets) {
                try {
                    pointService.expirePointsOfUser(pointsId);
                    success++;
                } catch (Exception e) {
                    failure++;
                    log.error("[PointExpiration] 단건 만료 실패 - pointsId: {}", pointsId, e);
                }
            }
            log.info("[PointExpiration] 완료 - 성공: {}, 실패: {}", success, failure);
        } finally {
            lock.unlock();
        }
    }
}
