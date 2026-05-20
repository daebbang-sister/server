package com.daebbang.daebbangcore.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReasonType {
    NOT_SATISFIED("상품이 맘에 들지 않아요"),
    WRONG_OPTION("옵션을 잘못 선택했어요"),
    WRONG_SIZE("사이즈가 맞지 않아요"),
    DEFECT_OR_DAMAGE("상품이 파손되었거나 불량이에요"),
    WRONG_ITEM("다른 상품이 왔거나 구성품이 빠졌어요"),
    DIFFERENT_FROM_DESC("상품 설명이 실제와 달라요");

    private final String description;
}
