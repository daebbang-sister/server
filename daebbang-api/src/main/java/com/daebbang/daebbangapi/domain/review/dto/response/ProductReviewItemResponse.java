package com.daebbang.daebbangapi.domain.review.dto.response;

import com.daebbang.daebbangcore.domain.review.entity.Review;
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
        String loginId = review.getUser().getLoginId();
        String masked = loginId.length() > 4
            ? loginId.substring(0, 4) + "*".repeat(loginId.length() - 4)
            : loginId + "****";

        return new ProductReviewItemResponse(
            review.getId(),
            masked,
            review.getCreatedAt(),
            review.getRating(),
            review.getContent(),
            review.getImages().stream().map(img -> img.getImageUrl()).toList(),
            review.getReply(),
            review.getReplyUpdatedAt()
        );
    }
}
