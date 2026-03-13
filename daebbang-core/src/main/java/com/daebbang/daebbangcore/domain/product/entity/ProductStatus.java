package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcommon.converter.BaseEnum;
import lombok.Getter;

@Getter
public enum ProductStatus implements BaseEnum<Integer> {
    // Default
    READY(1, "준비 중"),
    SALE(2, "판매 중"),
    SOLD_OUT(3, "품절"),

    // Admin용
    HIDDEN(4, "노출 제외"),
    DELETED(5, "상품 삭제"),

    // 그 외
    RESERVED(6, "예약 판매"),
    UPCOMING(7, "출시 예정");

    private final Integer value;
    private final String description;

    ProductStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
