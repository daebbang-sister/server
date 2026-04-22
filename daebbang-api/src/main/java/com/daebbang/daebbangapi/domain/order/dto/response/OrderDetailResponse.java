package com.daebbang.daebbangapi.domain.order.dto.response;

import com.daebbang.daebbangcore.domain.order.dto.OrderFullDetailResult;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
    String orderNumber,
    OrderStatus orderStatus,
    LocalDateTime orderedAt,
    int totalOriginalAmount,
    int totalSellingAmount,
    int shippingFee,
    int usedPoint,
    int paymentAmount,
    List<OrderDetailItem> details
) {
    public static OrderDetailResponse from(OrderFullDetailResult result) {
        return new OrderDetailResponse(
            result.orderNumber(),
            result.orderStatus(),
            result.orderedAt(),
            result.totalOriginalAmount(),
            result.totalSellingAmount(),
            result.shippingFee(),
            result.usedPoint(),
            result.paymentAmount(),
            result.details().stream().map(OrderDetailItem::from).toList()
        );
    }

    public record OrderDetailItem(
        Long orderDetailId,
        Long productDetailId,
        String productName,
        String imageUrl,
        String color,
        String colorCode,
        String size,
        int quantity,
        int originalPrice,
        int discountRate,
        int discountPrice,
        OrderDetailStatus status
    ) {
        public static OrderDetailItem from(OrderFullDetailResult.OrderDetailItem item) {
            return new OrderDetailItem(
                item.orderDetailId(),
                item.productDetailId(),
                item.productName(),
                item.imageUrl(),
                item.color(),
                item.colorCode(),
                item.size(),
                item.quantity(),
                item.originalPrice(),
                item.discountRate(),
                item.discountPrice(),
                item.status()
            );
        }
    }
}
