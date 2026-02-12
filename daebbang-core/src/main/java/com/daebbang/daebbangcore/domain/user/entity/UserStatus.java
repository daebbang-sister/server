package com.daebbang.daebbangcore.domain.user.entity;

import com.daebbang.daebbangcommon.converter.BaseEnum;
import lombok.Getter;

@Getter
public enum UserStatus implements BaseEnum<Integer> {
    ACTIVE(1, "활성"),
    DORMANT(2, "휴면"),
    WITHDRAWN(3, "탈퇴");

    private final Integer value;
    private final String description;

    UserStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}
