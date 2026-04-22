package com.daebbang.daebbangcore.domain.order.event;

import java.util.List;
import java.util.Objects;

public record StockInvalidateEvent(List<Long> productDetailIds) {

    public StockInvalidateEvent {
        Objects.requireNonNull(productDetailIds, "productDetailIds must not be null");
        productDetailIds = List.copyOf(productDetailIds);
    }
}
