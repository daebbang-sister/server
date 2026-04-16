package com.daebbang.daebbangcore.domain.order.command;

import java.util.List;

public record OrderPrepareCommand(
    Long userId,
    List<OrderItemCommand> items,
    int usedPoint
) {}
