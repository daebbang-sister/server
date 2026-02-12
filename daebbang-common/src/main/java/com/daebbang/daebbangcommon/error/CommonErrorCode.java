package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT_DATA(400, "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부에서 오류가 발생했습니다.");

    private final int status;
    private final String message;
}
