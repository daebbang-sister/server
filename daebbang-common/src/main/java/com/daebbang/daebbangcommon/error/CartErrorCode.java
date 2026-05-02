package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CartErrorCode implements ErrorCode {

    CART_NOT_FOUND(404, "존재하지 않는 카트 번호입니다.");

    private final int status;
    private final String message;
}
