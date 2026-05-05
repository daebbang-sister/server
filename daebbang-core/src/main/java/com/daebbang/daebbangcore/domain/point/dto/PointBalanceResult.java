package com.daebbang.daebbangcore.domain.point.dto;

public record PointBalanceResult(
    int currentAmount,
    int totalEarned,
    int totalUsed
) {
    public static PointBalanceResult zero() {
        return new PointBalanceResult(0, 0, 0);
    }
}
