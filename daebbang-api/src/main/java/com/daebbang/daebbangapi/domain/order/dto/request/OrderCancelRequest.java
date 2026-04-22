package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderCancelCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderCancelRequest(
    @NotBlank(message = "취소 사유를 입력해주세요.")
    @Size(max = 200, message = "취소 사유는 200자 이하로 입력해주세요.")
    String cancelReason
) {
    public OrderCancelCommand toCommand(Long userId, String orderNumber) {
        return new OrderCancelCommand(userId, orderNumber, cancelReason);
    }
}
