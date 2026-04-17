package com.daebbang.daebbangcore.domain.order.entity;

public enum OrderDetailStatus {
    NORMAL,            // 정상
    CANCEL_REQUESTED,  // 취소 요청
    CANCELLED,         // 취소 완료
    REFUND_REQUESTED,  // 반품 요청
    RETURNED           // 반품 완료
}
