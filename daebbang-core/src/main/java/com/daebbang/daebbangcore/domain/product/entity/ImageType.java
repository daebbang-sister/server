package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcommon.converter.BaseEnum;
import lombok.Getter;

@Getter
public enum ImageType implements BaseEnum<Integer> {

    MAIN(1, "메인"),
    HOVER(2, "호버"),
    DETAILS(3, "상세");

    private final Integer value;
    private final String description;

    ImageType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
