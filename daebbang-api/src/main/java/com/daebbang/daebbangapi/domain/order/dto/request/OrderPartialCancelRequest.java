package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderPartialCancelCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderPartialCancelRequest(
    @NotEmpty(message = "취소할 주문 항목을 하나 이상 선택해주세요.")
    List<Long> orderDetailIds,

    @NotBlank(message = "취소 사유를 입력해주세요.")
    @Size(max = 200, message = "취소 사유는 200자 이하로 입력해주세요.")
    String cancelReason
) {
    public OrderPartialCancelCommand toCommand(Long userId, String orderNumber) {
        return new OrderPartialCancelCommand(userId, orderNumber, orderDetailIds, cancelReason);
    }
}
