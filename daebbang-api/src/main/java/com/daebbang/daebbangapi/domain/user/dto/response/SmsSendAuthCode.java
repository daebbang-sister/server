package com.daebbang.daebbangapi.domain.user.dto.response;

public record SmsSendAuthCode(
    String authCode
) {
    public static SmsSendAuthCode toDto(String authCode) {
        return new SmsSendAuthCode(authCode);
    }
}
