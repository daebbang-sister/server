package com.daebbang.daebbangapi.domain.order.dto.response;

import com.daebbang.daebbangcore.domain.order.dto.OrderSummaryResult;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;

public record OrderListItemResponse(
    String orderNumber,
    OrderStatus orderStatus,
    LocalDateTime orderedAt,
    int paymentAmount,
    int totalQuantity,
    String representativeProductName,
    String representativeImageUrl,
    String representativeColor,
    String representativeSize,
    int representativeUnitPrice,
    int representativeQuantity
) {
    public static OrderListItemResponse from(OrderSummaryResult result) {
        return new OrderListItemResponse(
            result.orderNumber(),
            result.orderStatus(),
            result.orderedAt(),
            result.paymentAmount(),
            result.totalQuantity(),
            result.representativeProductName(),
            result.representativeImageUrl(),
            result.representativeColor(),
            result.representativeSize(),
            result.representativeUnitPrice(),
            result.representativeQuantity()
        );
    }
}
