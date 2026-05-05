package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointErrorCode implements ErrorCode {

    POINT_NOT_FOUND(404, "적립금 정보를 찾을 수 없습니다."),
    POINT_AMOUNT_INVALID(400, "유효하지 않은 적립금 금액입니다."),
    POINT_INSUFFICIENT_BALANCE(400, "보유한 적립금이 부족합니다."),
    POINT_USE_BELOW_MIN_ORDER(400, "3만원 이상 결제 시에만 적립금을 사용할 수 있습니다."),

    POINT_POLICY_NOT_FOUND(404, "적립금 정책을 찾을 수 없습니다."),
    POINT_POLICY_INVALID_VALUE(400, "적립금 정책 값이 올바르지 않습니다.");

    private final int status;
    private final String message;
}
