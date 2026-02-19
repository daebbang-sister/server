package com.daebbang.daebbangcommon.success;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserSuccessCode implements SuccessCode {

    USER_RETRIEVED(200, "회원 조회에 성공하였습니다."),
    USER_JOINED(201, "회원 가입에 성공하였습니다."),
    USER_UPDATED(200, "회원 정보 수정에 성공하였습니다.");

    private final int status;
    private final String message;
}
