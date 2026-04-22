package com.daebbang.daebbangcore.domain.order.event;

import java.util.List;

/**
 * 주문 취소 시 발행되는 이벤트.
 * @TransactionalEventListener(AFTER_COMMIT) 으로 소비 → 커밋 확정 후에만 Redis 캐시 무효화.
 * 트랜잭션 롤백 시에는 실행되지 않으므로 DB/Redis 정합성이 보장됨.
 */
public class StockInvalidateEvent {

    private final List<Long> productDetailIds;

    public StockInvalidateEvent(List<Long> productDetailIds) {
        this.productDetailIds = productDetailIds;
    }

    public List<Long> getProductDetailIds() {
        return productDetailIds;
    }
}
