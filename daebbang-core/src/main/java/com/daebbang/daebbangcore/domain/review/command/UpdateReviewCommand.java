package com.daebbang.daebbangcore.domain.review.command;

import com.daebbang.daebbangcore.infra.storage.UploadFile;
import java.util.List;

public record UpdateReviewCommand(
    Long userId,
    Long reviewId,
    int rating,
    String content,
    List<String> keepImageUrls,
    List<UploadFile> newImages
) {
}
