package com.daebbang.daebbangcore.domain.order.command;

import java.util.List;

public record OrderPartialCancelCommand(
    Long userId,
    String orderNumber,
    List<Long> orderDetailIds,
    String cancelReason
) {}
