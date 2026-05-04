package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    INVALID_PHONE_NUMBER(400, "전화번호 형식이 올바르지 않습니다."),
    INVALID_PHONE_NUMBER_FORMAT(400, "유효하지 않은 전화번호 형식입니다."),
    INVALID_USER_ID_FORMAT(400, "아이디 형식이 올바르지 않습니다."),
    INVALID_USER_PASSWORD_FORMAT(400, "비밀번호 형식이 올바르지 않습니다."),
    INVALID_USERNAME_FORMAT(400, "회원 이름 형식이 올바르지 않습니다."),
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    PASSWORD_CONFIRM_MISMATCH(400, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    SOCIAL_PASSWORD_NOT_ALLOWED(400, "소셜 로그인 회원은 비밀번호를 변경할 수 없습니다."),
    SAME_PHONE_NUMBER(400, "기존 전화번호와 동일합니다."),
    USER_NOT_FOUND(404, "존재하지 않는 사용자입니다."),
    DUPLICATE_PHONE_NUMBER(409, "이미 사용 중인 핸드폰 번호입니다."),
    DUPLICATE_LOGIN_ID(409, "이미 사용 중인 아이디입니다.");

    private final int status;
    private final String message;
}
