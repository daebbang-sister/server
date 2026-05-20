package com.daebbang.daebbangcore.domain.order.entity;

public enum OrderDetailStatus {
    NORMAL,            // 정상
    CANCEL_REQUESTED,  // 취소 요청
    CANCELLED,         // 취소 완료
    CLAIM_REQUESTED,   // 환불/교환 신청
    CLAIM_COMPLETED,   // 환불/교환 완료
    CLAIM_REJECTED     // 환불/교환 거절
}
