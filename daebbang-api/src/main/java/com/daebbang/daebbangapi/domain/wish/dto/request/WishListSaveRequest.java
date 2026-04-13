package com.daebbang.daebbangapi.domain.wish.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WishListSaveRequest(
    @NotNull(message = "상품 ID는 필수입니다.")
    @Positive(message = "상품 ID는 1 이상이어야 합니다.")
    Long productId
) {

}
