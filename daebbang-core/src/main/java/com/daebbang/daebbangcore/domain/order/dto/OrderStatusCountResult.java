package com.daebbang.daebbangcore.domain.order.dto;

public record OrderStatusCountResult(
    long paid,               // 결제완료 (PAID)
    long preparingDelivery,  // 배송준비중 (PREPARING_DELIVERY)
    long inDelivery,         // 배송중 (IN_DELIVERY)
    long delivered           // 배송완료 (DELIVERED)
) {}
