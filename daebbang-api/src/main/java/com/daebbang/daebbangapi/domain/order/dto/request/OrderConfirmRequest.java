package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderConfirmCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderConfirmRequest(
    @NotBlank(message = "주문 번호는 필수입니다.") String orderId,
    @NotBlank(message = "결제 키는 필수입니다.") String paymentKey,
    @Positive(message = "결제 금액은 0보다 커야 합니다.") int amount
) {
    public OrderConfirmCommand toCommand(Long userId) {
        return new OrderConfirmCommand(userId, orderId, paymentKey, amount);
    }
}
