package com.daebbang.daebbangcore.domain.order.command;

public record OrderConfirmCommand(
    Long userId,
    String orderNumber,
    String paymentKey,  // BANK_TRANSFER일 경우 null
    int amount
) {}
