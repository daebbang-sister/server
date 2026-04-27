package com.daebbang.daebbangcore.domain.review.entity;

import com.daebbang.daebbangcommon.converter.BaseEnum;
import lombok.Getter;

@Getter
public enum ReviewPointStatus implements BaseEnum<Integer> {
    PENDING(0, "적립금 대기"),
    APPROVED(1, "적립금 승인");

    private final Integer value;
    private final String description;

    ReviewPointStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
