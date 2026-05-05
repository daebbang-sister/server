package com.daebbang.daebbangcore.domain.point.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AmountType {

    FIXED("정액"),
    RATE("정률");

    private final String description;
}
