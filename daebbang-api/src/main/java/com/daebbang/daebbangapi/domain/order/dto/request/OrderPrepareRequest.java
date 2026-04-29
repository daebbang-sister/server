package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderPrepareRequest(
    @NotEmpty(message = "주문 상품 목록은 필수입니다.") List<@Valid OrderItemRequest> items,
    @Min(value = 0, message = "포인트는 0 이상이어야 합니다.") int usedPoint,
    @NotBlank(message = "수령인은 필수입니다.") String receiver,
    @NotBlank(message = "수령인 연락처는 필수입니다.") String receiverPhoneNumber,
    @NotBlank(message = "우편번호는 필수입니다.") String zipCode,
    @NotBlank(message = "주소는 필수입니다.") String address,
    @NotBlank(message = "상세주소는 필수입니다.") String detailAddress,
    @Min(value = 0, message = "배송비는 0 이상이어야 합니다.") int shippingFee,
    boolean isAddToAddressBook,
    @Nullable String addressAlias
) {
    public OrderPrepareCommand toCommand(Long userId) {
        return new OrderPrepareCommand(
            userId,
            items.stream().map(OrderItemRequest::toCommand).toList(),
            usedPoint,
            receiver,
            receiverPhoneNumber,
            zipCode,
            address,
            detailAddress,
            shippingFee,
            isAddToAddressBook,
            addressAlias
        );
    }
}
