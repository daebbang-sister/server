package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(404, "존재하지 않는 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(409, "이미 리뷰를 작성한 주문 항목입니다."),
    REVIEW_ALREADY_APPROVED(409, "적립금이 승인된 리뷰는 수정 또는 삭제할 수 없습니다."),
    REVIEW_POINT_CONFIG_NOT_FOUND(404, "리뷰 포인트 설정을 찾을 수 없습니다.");

    private final int status;
    private final String message;
}
