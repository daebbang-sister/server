package com.daebbang.daebbangapi.domain.point.dto.response;

import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;

public record PointBalanceResponse(
    int currentAmount,
    int totalEarned,
    int totalUsed
) {
    public static PointBalanceResponse from(PointBalanceResult result) {
        return new PointBalanceResponse(
            result.currentAmount(),
            result.totalEarned(),
            result.totalUsed()
        );
    }
}
