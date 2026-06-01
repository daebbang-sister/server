package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND(404, "존재하지 않는 상품입니다."),
    PRODUCT_DETAILS_NOT_FOUND(404, "존재하지 않는 상세 상품입니다."),
    INVALID_BEST_SETTINGS(400, "베스트 설정 값이 올바르지 않습니다.");

    private final int status;
    private final String message;
}
