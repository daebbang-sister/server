package com.daebbang.daebbangcore.domain.order.command;

public record OrderItemCommand(
    Long productDetailId,
    int quantity
) {}
