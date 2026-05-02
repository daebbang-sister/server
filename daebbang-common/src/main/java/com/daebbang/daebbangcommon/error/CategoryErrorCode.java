package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    CATEGORY_NOT_FOUND(404, "존재하지 않는 카테고리입니다.");

    private final int status;
    private final String message;
}
