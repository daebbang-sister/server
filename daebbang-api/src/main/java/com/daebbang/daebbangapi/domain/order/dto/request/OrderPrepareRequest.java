package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderPrepareRequest(
    @NotEmpty List<@Valid OrderItemRequest> items,
    @Min(0) int usedPoint
) {
    public OrderPrepareCommand toCommand(Long userId) {
        return new OrderPrepareCommand(
            userId,
            items.stream().map(OrderItemRequest::toCommand).toList(),
            usedPoint
        );
    }
}
