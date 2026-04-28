package com.daebbang.daebbangapi.domain.review.dto.response;

import com.daebbang.daebbangcommon.util.MaskingUtils;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.entity.ReviewImage;
import java.time.LocalDateTime;
import java.util.List;

public record ProductReviewItemResponse(
    Long reviewId,
    String maskedLoginId,
    LocalDateTime createdAt,
    int rating,
    String content,
    List<String> imageUrls,
    String reply,
    LocalDateTime replyUpdatedAt
) {
    public static ProductReviewItemResponse from(Review review) {
        return new ProductReviewItemResponse(
            review.getId(),
            MaskingUtils.maskReviewerId(review.getUser().getLoginId()),
            review.getCreatedAt(),
            review.getRating(),
            review.getContent(),
            review.getImages().stream().map(ReviewImage::getImageUrl).toList(),
            review.getReply(),
            review.getReplyUpdatedAt()
        );
    }
}
