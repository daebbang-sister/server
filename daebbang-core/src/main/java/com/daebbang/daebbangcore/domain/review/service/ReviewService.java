package com.daebbang.daebbangcore.domain.review.service;

import com.daebbang.daebbangcore.domain.review.command.CreateReviewCommand;
import com.daebbang.daebbangcore.domain.review.command.UpdateReviewCommand;
import com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    void createReview(CreateReviewCommand command);

    void updateReview(UpdateReviewCommand command);

    void deleteReview(Long userId, Long reviewId);

    Page<@NonNull Review> getMyReviews(Long userId, Pageable pageable);

    Page<@NonNull Review> getProductReviews(Long productId, Pageable pageable);

    ReviewStatsResult getProductReviewStats(Long productId);

    ReviewPointConfig getPointConfig();
}
