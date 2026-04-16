package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderConfirmCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderConfirmRequest(
    @NotBlank String orderId,
    @NotBlank String paymentKey,
    @Positive int amount
) {
    public OrderConfirmCommand toCommand(Long userId) {
        return new OrderConfirmCommand(userId, orderId, paymentKey, amount);
    }
}
