package com.daebbang.daebbangcore.domain.order.command;

public record OrderCancelCommand(
    Long userId,
    String orderNumber,
    String cancelReason
) {}
