package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
    @NotNull(message = "상품 상세 ID는 비어있어서는 안됩니다.")
    @Positive
    Long productDetailId,

    @Min(value = 1, message = "최소 1개 수량 이상 선택하셔야 합니다.")
    int quantity
) {
    public OrderItemCommand toCommand() {
        return new OrderItemCommand(productDetailId, quantity);
    }
}
