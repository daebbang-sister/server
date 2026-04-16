package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
    @NotNull Long productDetailId,
    @Min(1) int quantity
) {
    public OrderItemCommand toCommand() {
        return new OrderItemCommand(productDetailId, quantity);
    }
}
