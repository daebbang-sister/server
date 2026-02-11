package com.daebbang.daebbangcore.domain.user.entity;

import lombok.Getter;

@Getter
public enum Provider {
    LOCAL("local"),
    KAKAO("kakao");

    private final String description;

    Provider(String description) {
        this.description = description;
    }
}
