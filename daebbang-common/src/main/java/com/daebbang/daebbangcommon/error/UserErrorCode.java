package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_PHONE_NUMBER_FORMAT(400, "유효하지 않은 전화번호 형식입니다."),
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    DUPLICATE_LOGIN_ID(409, "이미 사용 중인 아이디입니다.");

    private final int status;
    private final String message;
}
