package com.daebbang.daebbangcore.domain.review.scheduler;

import com.daebbang.daebbangcore.domain.review.service.ReviewService;
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
public class ReviewPointApprovalScheduler {

    private static final String SCHEDULER_LOCK_KEY = "scheduler:review-point-approval:lock";

    private final ReviewService reviewService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 3 * * *")
    public void approvePendingReviews() {
        RLock lock = redissonClient.getLock(SCHEDULER_LOCK_KEY);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, 600, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.info("[ReviewPointApproval] 다른 인스턴스가 실행 중 - 스킵");
            return;
        }

        try {
            List<Long> targets = reviewService.findPendingReviewIdsToApprove();
            if (targets.isEmpty()) {
                return;
            }

            log.info("[ReviewPointApproval] 자동 승인 대상 - {}건", targets.size());

            int success = 0;
            int failure = 0;
            for (Long reviewId : targets) {
                try {
                    reviewService.approveReviewPoint(reviewId);
                    success++;
                } catch (Exception e) {
                    failure++;
                    log.error("[ReviewPointApproval] 단건 승인 실패 - reviewId: {}", reviewId, e);
                }
            }
            log.info("[ReviewPointApproval] 완료 - 성공: {}, 실패: {}", success, failure);
        } finally {
            lock.unlock();
        }
    }
}
