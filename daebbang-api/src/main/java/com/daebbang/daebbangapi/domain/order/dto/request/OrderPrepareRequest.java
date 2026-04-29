package com.daebbang.daebbangapi.domain.order.dto.request;

import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OrderPrepareRequest(
    @NotEmpty(message = "주문 상품 목록은 필수입니다.") List<@Valid OrderItemRequest> items,
    @Min(value = 0, message = "포인트는 0 이상이어야 합니다.") int usedPoint,
    @NotBlank(message = "수령인은 필수입니다.")
    @Size(max = 50, message = "수령인 이름은 50자 이내여야 합니다.") String receiver,
    @NotBlank(message = "수령인 연락처는 필수입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "유효하지 않은 전화번호 형식입니다.") String receiverPhoneNumber,
    @NotBlank(message = "우편번호는 필수입니다.")
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.") String zipCode,
    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 200, message = "주소는 200자 이내여야 합니다.") String address,
    @NotBlank(message = "상세주소는 필수입니다.")
    @Size(max = 100, message = "상세주소는 100자 이내여야 합니다.") String detailAddress,
    @Min(value = 0, message = "배송비는 0 이상이어야 합니다.") int shippingFee,
    @Nullable @Size(max = 100, message = "배송 요청사항은 100자 이내여야 합니다.") String orderNote,
    boolean isAddToAddressBook,
    boolean isDefaultAddress,
    @Nullable @Size(max = 50, message = "주소록 별칭은 50자 이내여야 합니다.") String addressAlias
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
            orderNote,
            isAddToAddressBook,
            isDefaultAddress,
            addressAlias
        );
    }
}
