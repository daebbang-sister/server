package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcommon.converter.BaseEnum;
import lombok.Getter;

@Getter
public enum DiscountType implements BaseEnum<Integer> {

    NONE(1, "없음"),
    PERIOD(2, "기간 지정 할인"),
    ALWAYS(3, "상시 할인");

    private final Integer value;
    private final String description;

    DiscountType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
