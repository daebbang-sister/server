package com.daebbang.daebbangcore.domain.review.command;

import java.util.List;

public record CreateReviewCommand(
    Long userId,
    Long orderDetailId,
    int rating,
    String content,
    List<String> imageUrls
) {
}
