package com.daebbang.daebbangapi.domain.wish.dto.response;

public record WishCheckInfo(
    boolean isWished,
    Long wishId
) {
}
