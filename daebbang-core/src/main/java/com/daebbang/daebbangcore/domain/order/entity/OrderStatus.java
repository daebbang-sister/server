package com.daebbang.daebbangcore.domain.order.entity;

public enum OrderStatus {
    PENDING,          // 결제 대기
    WAITING_DEPOSIT,  // 입금 대기 (가상계좌)
    PAID,             // 결제 완료
    IN_DELIVERY,      // 배송중
    DELIVERED,        // 배송완료
    COMPLETED,        // 구매확정
    CANCELLED,        // 취소
    EXPIRED           // TTL 만료
}
