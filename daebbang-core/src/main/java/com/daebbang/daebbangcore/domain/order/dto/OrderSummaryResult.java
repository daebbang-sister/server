package com.daebbang.daebbangcore.domain.order.dto;

import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;

public record OrderSummaryResult(
    Long id,
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
) {}
