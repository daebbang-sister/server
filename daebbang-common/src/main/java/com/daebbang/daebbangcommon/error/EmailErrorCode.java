package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailErrorCode implements ErrorCode {

    INVALID_EMAIL_FORMAT(400, "유효하지 않은 이메일 형식입니다."),
    EMAIL_SEND_TIMEOUT(408, "이메일 발송 시간이 초과되었습니다."),
    DAILY_EMAIL_LIMIT_EXCEEDED(429, "일일 이메일 발송 횟수가 초과되었습니다."),
    EMAIL_SEND_FAILED(500, "이메일 발송에 실패했습니다. 관리자에게 문의하세요.");

    private final int status;
    private final String message;
}
