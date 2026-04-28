package com.daebbang.daebbangapi.domain.review.dto.response;

import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.entity.ReviewImage;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import java.time.LocalDateTime;
import java.util.List;

public record MyReviewItemResponse(
    Long reviewId,
    Long productId,
    String productName,
    LocalDateTime createdAt,
    int rating,
    String content,
    List<String> imageUrls,
    String reply,
    LocalDateTime replyUpdatedAt,
    String pointStatus,
    int expectedPoint
) {
    public static MyReviewItemResponse from(Review review, ReviewPointConfig config) {
        int expectedPoint = review.isPhotoReview()
            ? config.getPhotoReviewPoint()
            : config.getNormalReviewPoint();

        return new MyReviewItemResponse(
            review.getId(),
            review.getProduct().getId(),
            review.getProduct().getProductName(),
            review.getCreatedAt(),
            review.getRating(),
            review.getContent(),
            review.getImages().stream().map(ReviewImage::getImageUrl).toList(),
            review.getReply(),
            review.getReplyUpdatedAt(),
            review.isApproved() ? "적립금 승인" : "적립금 대기",
            expectedPoint
        );
    }
}
