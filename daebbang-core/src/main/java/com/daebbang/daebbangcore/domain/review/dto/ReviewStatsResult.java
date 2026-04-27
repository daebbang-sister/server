package com.daebbang.daebbangcore.domain.review.dto;

import java.util.Map;

public record ReviewStatsResult(
    long totalCount,
    double averageRating,
    Map<Integer, Long> ratingCounts
) {
}
