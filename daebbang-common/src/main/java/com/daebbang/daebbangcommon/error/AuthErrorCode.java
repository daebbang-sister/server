package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(401, "지원되지 않는 토큰 형식입니다."),
    INVALID_LOGIN_INFO(401, "아이디 또는 비밀번호가 일치하지 않습니다.");

    private final int status;
    private final String message;
}
