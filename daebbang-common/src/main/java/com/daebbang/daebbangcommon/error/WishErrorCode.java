package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WishErrorCode implements ErrorCode {

    WISH_LIST_NOT_FOUND(404, "존재하지 않는 위시리스트 항목입니다."),
    WISH_LIST_ALREADY_EXISTS(409, "이미 위시리스트에 추가된 상품입니다.");

    private final int status;
    private final String message;
}
