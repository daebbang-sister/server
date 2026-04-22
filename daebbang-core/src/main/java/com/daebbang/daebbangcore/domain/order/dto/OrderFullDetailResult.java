package com.daebbang.daebbangcore.domain.order.dto;

import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record OrderFullDetailResult(
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
    ) {}
}
