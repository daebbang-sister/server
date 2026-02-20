package com.daebbang.daebbangcommon.dto.authority;

public record TokenInfo(
    String grantType,
    String accessToken,
    String refreshToken) {

    public static TokenInfo create(String accessToken, String refreshToken) {
        return new TokenInfo("bearer", accessToken, refreshToken);
    }

    public static TokenInfo create(String grantType, String accessToken, String refreshToken) {
        return new TokenInfo(grantType, accessToken, refreshToken);
    }
}
