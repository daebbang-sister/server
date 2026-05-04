package com.daebbang.daebbangcore.domain.review.command;

import com.daebbang.daebbangcore.infra.storage.UploadFile;
import java.util.List;

public record CreateReviewCommand(
    Long userId,
    Long orderDetailId,
    int rating,
    String content,
    List<UploadFile> images
) {
}
