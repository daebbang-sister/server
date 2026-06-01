package com.daebbang.daebbangcore.domain.product.event;

import com.daebbang.daebbangcore.domain.product.repository.ProductStatsRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStatsEventListener {

    private final ProductStatsRedisRepository productStatsRedisRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductViewedEvent(ProductViewedEvent event) {
        try {
            productStatsRedisRepository.increment(event.productId(), "view_count", 1);
            log.debug("[ProductStats] 조회수 증가 - productId: {}", event.productId());
        } catch (Exception e) {
            log.error("[ProductStats] 조회수 증가 실패 - productId: {}", event.productId(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductStatsEvent(ProductStatsEvent event) {
        try {
            productStatsRedisRepository.increment(event.productId(), event.field(), event.delta());
            log.debug("[ProductStats] 통계 업데이트 - productId: {}, field: {}, delta: {}",
                event.productId(), event.field(), event.delta());
        } catch (Exception e) {
            log.error("[ProductStats] 통계 업데이트 실패 - productId: {}, field: {}", event.productId(), event.field(), e);
        }
    }
}
