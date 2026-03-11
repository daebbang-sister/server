package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    JSON_CONVERT_ERROR(400, "Json Body 파싱 중 문제가 발생했습니다."),
    INVALID_INPUT_DATA(400, "잘못된 입력값입니다."),
    INVALID_AUTHENTICATION(401, "인증이 필요합니다."),
    INVALID_AUTHORIZATION(403, "권한이 부족합니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부에서 오류가 발생했습니다."),
    CANNOT_INSTANTIATE_UTIL_CLASS(500, "유틸리티 클래스는 인스턴스화할 수 없습니다.");

    private final int status;
    private final String message;
}
