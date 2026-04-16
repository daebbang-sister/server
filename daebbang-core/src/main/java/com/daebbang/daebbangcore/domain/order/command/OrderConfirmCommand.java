package com.daebbang.daebbangcore.domain.order.command;

public record OrderConfirmCommand(
    Long userId,
    String orderNumber,
    String paymentKey,
    int amount
) {}
