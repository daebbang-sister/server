package com.daebbang.daebbangapi.domain.cart.dto.request;

import com.daebbang.daebbangcore.domain.cart.command.CartSaveCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartSaveRequest(
    @NotNull(message = "상품 상세 아이디는 비어있어서는 안됩니다.")
    Long productDetailId,
    @NotNull(message = "수량은 비어있어서는 안됩니다.")
    @Min(value = 1, message = "수량은 최소 한 개 이상 선택해야 합니다.")
    Integer quantity
) {

    public CartSaveCommand toCommand() {
        return new CartSaveCommand(productDetailId, quantity);
    }
}
