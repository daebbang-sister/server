package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SmsErrorCode implements ErrorCode {

    AUTH_CODE_MISMATCH(400, "인증번호 코드가 일치하지 않습니다."),
    AUTH_CODE_EXPIRED(401, "인증번호가 만료되었거나 일치하지 않습니다."),
    EXCEEDED_SENDING_LIMIT(429, "인증번호 발송 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요."),
    SMS_SEND_FAILED(500, "인증문자 발송에 실패했습니다. 관리자에게 문의하세요.");

    private final int status;
    private final String message;
}
