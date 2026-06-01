package com.daebbang.daebbangcore.domain.product.scheduler;

import com.daebbang.daebbangcore.domain.product.entity.ProductStats;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.repository.ProductRepository;
import com.daebbang.daebbangcore.domain.product.repository.ProductSalesLogRepository;
import com.daebbang.daebbangcore.domain.product.repository.ProductStatsRedisRepository;
import com.daebbang.daebbangcore.domain.product.repository.ProductStatsRepository;
import com.daebbang.daebbangcore.domain.product.service.ProductBestSettingsService;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStatsFlushScheduler {

    private static final String LOCK_KEY = "scheduler:product-stats-flush:lock";
    private static final String CLEANUP_LOCK_KEY = "scheduler:product-sales-log-cleanup:lock";

    private final ProductStatsRedisRepository productStatsRedisRepository;
    private final ProductStatsRepository productStatsRepository;
    private final ProductSalesLogRepository productSalesLogRepository;
    private final ProductRepository productRepository;
    private final ProductBestSettingsService productBestSettingsService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void flushProductStats() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.info("[ProductStatsFlush] 다른 인스턴스가 실행 중 - 스케줄러 스킵");
            return;
        }

        try {
            Set<Long> productIds = productStatsRedisRepository.getTrackedProductIds();
            if (productIds.isEmpty()) {
                return;
            }

            log.info("[ProductStatsFlush] 스케줄러 실행 - 대상 상품 수: {}", productIds.size());

            for (Long productId : productIds) {
                try {
                    Map<String, Long> delta = productStatsRedisRepository.getAndClear(productId);
                    if (delta.isEmpty()) {
                        continue;
                    }

                    Optional<ProductStats> statsOpt = productStatsRepository.findByProductId(productId);
                    ProductStats stats;
                    if (statsOpt.isPresent()) {
                        stats = statsOpt.get();
                    } else {
                        Optional<Products> productOpt = productRepository.findById(productId);
                        if (productOpt.isEmpty()) {
                            log.warn("[ProductStatsFlush] 존재하지 않는 상품 - productId: {}", productId);
                            continue;
                        }
                        stats = ProductStats.create(productOpt.get());
                    }

                    if (delta.containsKey("view_count")) {
                        stats.addViewCount(delta.get("view_count").intValue());
                    }
                    if (delta.containsKey("wish_count")) {
                        stats.addWishCount(delta.get("wish_count").intValue());
                    }
                    if (delta.containsKey("sales_count")) {
                        long salesDelta = delta.get("sales_count");
                        stats.addSalesCount((int) salesDelta);
                        productSalesLogRepository.upsertSalesLog(productId, LocalDate.now(), salesDelta);
                    }
                    if (delta.containsKey("review_count")) {
                        stats.addReviewCount(delta.get("review_count").intValue());
                    }

                    stats.update();
                    productStatsRepository.save(stats);

                } catch (Exception e) {
                    log.error("[ProductStatsFlush] 상품 통계 플러시 실패 - productId: {}", productId, e);
                }
            }

            log.info("[ProductStatsFlush] 스케줄러 완료 - 처리 상품 수: {}", productIds.size());

        } finally {
            lock.unlock();
        }
    }

    /**
     * 보존 기간이 지난 일별 판매 로그를 정리한다. (매일 새벽 4시 30분)
     * product_sales_log 는 날짜별로 계속 쌓이므로 주기적으로 오래된 행을 제거하여 용량을 관리한다.
     */
    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void cleanupOldSalesLog() {
        RLock lock = redissonClient.getLock(CLEANUP_LOCK_KEY);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!acquired) {
            log.info("[ProductSalesLogCleanup] 다른 인스턴스가 실행 중 - 스케줄러 스킵");
            return;
        }

        try {
            // 보존 기간 = 베스트 조회 최대 기간(설정값, SSOT). 이보다 오래된 로그는 베스트 계산에 쓰이지 않는다.
            // 누적 총 판매량은 product_stats.sales_count 에 영구 보존되므로 데이터 손실은 없다.
            int retentionDays = productBestSettingsService.getMaxPeriodDays();
            LocalDate threshold = LocalDate.now().minusDays(retentionDays);
            int deleted = productSalesLogRepository.deleteBySaleDateBefore(threshold);
            log.info("[ProductSalesLogCleanup] 오래된 판매 로그 정리 완료 - 기준일: {} 이전, 삭제 건수: {}",
                threshold, deleted);
        } finally {
            lock.unlock();
        }
    }
}
