package com.daebbang.daebbangapi.domain.users.dto.request;

public record SmsVerifyRequest(
    String phoneNumber,
    String authCode
) {

}
