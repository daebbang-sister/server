package com.daebbang.daebbangapi.domain.review.dto.request;

import com.daebbang.daebbangcore.domain.review.command.UpdateReviewCommand;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateReviewRequest(
    @NotNull(message = "별점은 필수입니다.")
    @Min(value = 1, message = "별점은 최소 1점입니다.")
    @Max(value = 5, message = "별점은 최대 5점입니다.")
    Integer rating,

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 20, max = 300, message = "리뷰 내용은 최소 20자, 최대 300자입니다.")
    String content,

    List<String> keepImageUrls
) {
    public UpdateReviewCommand toCommand(Long userId, Long reviewId, List<UploadFile> newImages) {
        return new UpdateReviewCommand(userId, reviewId, rating, content, keepImageUrls, newImages);
    }
}
