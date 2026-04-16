package com.daebbang.daebbangcore.domain.order.dto;

public record OrderPrepareResponse(
    String orderNumber,
    int paymentAmount
) {}
