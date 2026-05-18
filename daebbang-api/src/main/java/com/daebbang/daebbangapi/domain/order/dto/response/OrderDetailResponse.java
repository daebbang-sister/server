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
    int expectedPoint,
    int earnedPoint,
    OrdererInfo ordererInfo,
    ShippingInfo shippingInfo,
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
            result.expectedPoint(),
            result.earnedPoint(),
            OrdererInfo.from(result.ordererInfo()),
            ShippingInfo.from(result.shippingInfo()),
            result.details().stream().map(OrderDetailItem::from).toList()
        );
    }

    public record OrdererInfo(
        String name,
        String maskedPhone
    ) {
        public static OrdererInfo from(OrderFullDetailResult.OrdererInfo info) {
            return new OrdererInfo(info.name(), info.maskedPhone());
        }
    }

    public record ShippingInfo(
        String receiver,
        String zipCode,
        String address,
        String detailAddress,
        String maskedPhone,
        String orderNote
    ) {
        public static ShippingInfo from(OrderFullDetailResult.ShippingInfo info) {
            return new ShippingInfo(
                info.receiver(),
                info.zipCode(),
                info.address(),
                info.detailAddress(),
                info.maskedPhone(),
                info.orderNote()
            );
        }
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
