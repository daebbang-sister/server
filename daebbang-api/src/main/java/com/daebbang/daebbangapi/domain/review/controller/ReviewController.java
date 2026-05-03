package com.daebbang.daebbangapi.domain.review.controller;

import com.daebbang.daebbangapi.domain.review.dto.request.CreateReviewRequest;
import com.daebbang.daebbangapi.domain.review.dto.request.UpdateReviewRequest;
import com.daebbang.daebbangapi.domain.review.dto.response.MyReviewItemResponse;
import com.daebbang.daebbangapi.domain.review.support.UploadFileMapper;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.page.PageResponse;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import com.daebbang.daebbangcore.domain.review.service.ReviewService;
import com.daebbang.daebbangcore.infra.storage.UploadFile;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NonNull CommonResponse<Void>> createReview(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestPart("data") CreateReviewRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<UploadFile> uploadFiles = UploadFileMapper.toUploadFiles(images);
        reviewService.createReview(request.toCommand(userId, uploadFiles));
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CommonResponse.success(UserSuccessCode.REVIEW_CREATED));
    }

    @PutMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<@NonNull CommonResponse<Void>> updateReview(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long reviewId,
        @Valid @RequestPart("data") UpdateReviewRequest request,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        List<UploadFile> uploadFiles = UploadFileMapper.toUploadFiles(images);
        reviewService.updateReview(request.toCommand(userId, reviewId, uploadFiles));
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.REVIEW_UPDATED));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<@NonNull CommonResponse<Void>> deleteReview(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.REVIEW_DELETED));
    }

    @GetMapping("/my")
    public ResponseEntity<@NonNull CommonResponse<PageResponse<MyReviewItemResponse>>> getMyReviews(
        @AuthenticationPrincipal Long userId,
        @PageableDefault(size = 10) Pageable pageable
    ) {
        ReviewPointConfig config = reviewService.getPointConfig();
        PageResponse<MyReviewItemResponse> response = PageResponse.from(
            reviewService.getMyReviews(userId, pageable)
                .map(review -> MyReviewItemResponse.from(review, config))
        );
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.REVIEW_LIST_RETRIEVED, response));
    }
}
