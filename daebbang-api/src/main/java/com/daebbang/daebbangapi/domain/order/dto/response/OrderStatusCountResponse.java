package com.daebbang.daebbangapi.domain.order.dto.response;

import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;

public record OrderStatusCountResponse(
    long paid,               // 결제완료
    long preparingDelivery,  // 배송준비중
    long inDelivery,         // 배송중
    long delivered           // 배송완료
) {
    public static OrderStatusCountResponse from(OrderStatusCountResult result) {
        return new OrderStatusCountResponse(
            result.paid(),
            result.preparingDelivery(),
            result.inDelivery(),
            result.delivered()
        );
    }
}
