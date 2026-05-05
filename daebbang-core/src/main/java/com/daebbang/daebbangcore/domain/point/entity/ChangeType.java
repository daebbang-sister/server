package com.daebbang.daebbangcore.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChangeType {

    EARN_SIGNUP("회원가입 적립", true),
    EARN_REVIEW("리뷰 적립", true),
    EARN_PURCHASE("구매 확정 적립", true),
    USE_PAYMENT("결제 사용", false),
    REFUND_CANCEL("주문 취소 환불", true),
    REFUND_REVERSE("환불에 따른 적립 회수", false),
    EXPIRE("소멸", false);

    private final String description;
    private final boolean earn;
}
