package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AddressErrorCode implements ErrorCode {

    ADDRESS_NOT_FOUND(404, "존재하지 않는 주소입니다."),
    ADDRESS_LIMIT_EXCEEDED(409, "주소록은 최대 5개까지 등록 가능합니다.");

    private final int status;
    private final String message;
}
