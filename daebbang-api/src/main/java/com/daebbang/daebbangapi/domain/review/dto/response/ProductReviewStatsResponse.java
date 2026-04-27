package com.daebbang.daebbangapi.domain.review.dto.response;

import com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult;
import java.util.Map;

public record ProductReviewStatsResponse(
    long totalCount,
    double averageRating,
    Map<Integer, Long> ratingCounts
) {
    public static ProductReviewStatsResponse from(ReviewStatsResult result) {
        return new ProductReviewStatsResponse(
            result.totalCount(),
            result.averageRating(),
            result.ratingCounts()
        );
    }
}
