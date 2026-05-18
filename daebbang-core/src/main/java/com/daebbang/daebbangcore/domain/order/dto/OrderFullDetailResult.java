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
    int expectedPoint,
    int earnedPoint,
    OrdererInfo ordererInfo,
    ShippingInfo shippingInfo,
    List<OrderDetailItem> details
) {
    public record OrdererInfo(
        String name,
        String maskedPhone
    ) {}

    public record ShippingInfo(
        String receiver,
        String zipCode,
        String address,
        String detailAddress,
        String maskedPhone,
        String orderNote
    ) {}

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
