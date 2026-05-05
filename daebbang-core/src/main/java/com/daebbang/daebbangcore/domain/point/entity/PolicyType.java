package com.daebbang.daebbangcore.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicyType {

    SIGNUP("회원가입 적립"),
    REVIEW_TEXT("일반 리뷰 적립"),
    REVIEW_PHOTO("포토 리뷰 적립"),
    PURCHASE("구매 확정 적립");

    private final String description;
}
