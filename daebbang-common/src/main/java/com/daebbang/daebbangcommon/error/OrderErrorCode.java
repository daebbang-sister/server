package com.daebbang.daebbangcommon.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(404, "존재하지 않는 주문입니다."),
    ORDER_AMOUNT_MISMATCH(400, "결제 금액이 주문 금액과 일치하지 않습니다."),
    ORDER_USER_MISMATCH(403, "주문자 정보가 일치하지 않습니다."),
    ORDER_ALREADY_PROCESSED(409, "이미 처리된 주문입니다."),
    ORDER_SHIPPING_FEE_INVALID(400, "배송비가 올바르지 않습니다."),
    ORDER_CANCEL_NOT_ALLOWED(400, "취소할 수 없는 주문 상태입니다."),
    ORDER_PARTIAL_CANCEL_INVALID(400, "유효하지 않은 취소 항목입니다."),
    ORDER_DATE_RANGE_INVALID(400, "조회 시작일이 종료일보다 늦을 수 없습니다."),
    ORDER_DETAIL_NOT_FOUND(404, "존재하지 않는 주문 상세입니다."),
    ORDER_NOT_COMPLETED(400, "주문 완료 상태에서만 리뷰를 작성할 수 있습니다."),

    OUT_OF_STOCK(409, "재고가 부족합니다."),
    STOCK_LOCK_FAILED(503, "재고 처리 중입니다. 잠시 후 다시 시도해주세요."),
    STOCK_RESTORE_FAILED(500, "재고 복원에 실패했습니다. 관리자에게 문의하세요."),

    PAYMENT_CONFIRMATION_FAILED(502, "결제 승인에 실패했습니다. 관리자에게 문의하세요."),
    PAYMENT_NOT_FOUND(404, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_CANCEL_FAILED(502, "결제 취소에 실패했습니다. 관리자에게 문의하세요."),

    POINT_EXCEEDS_PAYMENT(400, "포인트가 결제 금액을 초과합니다.");

    private final int status;
    private final String message;
}
