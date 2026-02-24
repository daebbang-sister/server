package com.daebbang.daebbangapi.domain.users.dto.response;

public record SmsSendAuthCode(
    String authCode
) {
    public static SmsSendAuthCode toDto(String authCode) {
        return new SmsSendAuthCode(authCode);
    }
}
