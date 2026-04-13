package com.daebbang.daebbangapi.domain.wish.dto.request;

import jakarta.validation.constraints.NotNull;

public record WishListSaveRequest(
    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId
) {

}
