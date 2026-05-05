package com.daebbang.daebbangcore.domain.review.dto;

public record ReviewExpectedAmounts(
    int normalReviewPoint,
    int photoReviewPoint
) {
    public int resolve(boolean isPhotoReview) {
        return isPhotoReview ? photoReviewPoint : normalReviewPoint;
    }
}
