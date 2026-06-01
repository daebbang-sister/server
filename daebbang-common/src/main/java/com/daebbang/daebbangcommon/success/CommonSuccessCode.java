package com.daebbang.daebbangcommon.success;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonSuccessCode implements SuccessCode {

    SELECT_SUCCESS(200, "조회에 성공하였습니다."),
    UPDATE_SUCCESS(200, "수정에 성공하였습니다."),
    DELETE_SUCCESS(200, "삭제에 성공하였습니다."),
    CREATE_SUCCESS(201, "생성에 성공하였습니다.");

    private final int status;
    private final String message;
}
