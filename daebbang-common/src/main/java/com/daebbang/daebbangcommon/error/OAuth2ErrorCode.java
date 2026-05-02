package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OAuth2ErrorCode implements ErrorCode {

    UNSUPPORTED_SOCIAL_PROVIDER(400, "지원하지 않는 소셜 로그인 제공자입니다."),
    INVALID_OAUTH2_ATTRIBUTES(400, "소셜 서비스로부터 사용자 정보를 가져올 수 없습니다."),
    OAUTH2_AUTHENTICATION_FAILED(401, "소셜 로그인 인증에 실패했습니다.");

    private final int status;
    private final String message;
}
