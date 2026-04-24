package com.daebbang.daebbangapi.domain.wish.dto.response;

public record WishIdInfo(
    Long wishId
) {
    public static WishIdInfo toDto(Long wishId) {
        return new WishIdInfo(wishId);
    }
}
