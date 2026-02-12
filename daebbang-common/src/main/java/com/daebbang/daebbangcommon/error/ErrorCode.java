package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_DATA(400, "잘못된 입력값입니다.");

    private final int status;
    private final String message;
}
