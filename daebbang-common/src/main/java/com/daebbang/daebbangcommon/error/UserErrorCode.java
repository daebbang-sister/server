package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(401, "지원되지 않는 토큰 형식입니다."),

    UNSUPPORTED_SOCIAL_PROVIDER(400, "지원하지 않는 소셜 로그인 제공자입니다."),
    INVALID_OAUTH2_ATTRIBUTES(400, "소셜 서비스로부터 사용자 정보를 가져올 수 없습니다."),
    OAUTH2_AUTHENTICATION_FAILED(401, "소셜 로그인 인증에 실패했습니다."),

    INVALID_PHONE_NUMBER(400, "전화번호 형식이 올바르지 않습니다."),
    AUTH_CODE_MISMATCH(400, "인증번호 코드가 일치하지 않습니다."),
    AUTH_CODE_EXPIRED(401, "인증번호가 만료되었거나 일치하지 않습니다."),
    EXCEEDED_SENDING_LIMIT(429, "인증번호 발송 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."),
    SMS_SEND_FAILED(500, "인증문자 발송에 실패했습니다. 관리자에게 문의하세요."),

    INVALID_PHONE_NUMBER_FORMAT(400, "유효하지 않은 전화번호 형식입니다."),
    INVALID_LOGIN_INFO(401, "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    USER_NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    DUPLICATE_PHONE_NUMBER(409, "이미 사용 중인 핸드폰 번호입니다."),
    DUPLICATE_LOGIN_ID(409, "이미 사용 중인 아이디입니다.");

    private final int status;
    private final String message;
}
