package com.daebbang.daebbangcommon.dto.authority;

import lombok.Getter;

@Getter
public class TokenInfo {

    private final String grantType;
    private final String accessToken;
    private final String refreshToken;

    private TokenInfo(String grantType, String accessToken, String refreshToken) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenInfo create(String grantType, String accessToken, String refreshToken) {
        return new TokenInfo(grantType, accessToken, refreshToken);
    }
}
