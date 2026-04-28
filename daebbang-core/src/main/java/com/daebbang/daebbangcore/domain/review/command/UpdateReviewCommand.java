package com.daebbang.daebbangcore.domain.review.command;

import java.util.List;

public record UpdateReviewCommand(
    Long userId,
    Long reviewId,
    int rating,
    String content,
    List<String> imageUrls
) {
}
