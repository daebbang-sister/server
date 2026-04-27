package com.daebbang.daebbangcore.domain.review.repository.dsl;

import com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ReviewCustomRepository {

    Page<@NonNull Review> findActiveReviewsByProductId(Long productId, Pageable pageable);

    Page<@NonNull Review> findActiveReviewsByUserId(Long userId, Pageable pageable);

    Page<@NonNull Review> findActiveReviewsForAdmin(Pageable pageable);

    ReviewStatsResult getReviewStats(Long productId);
}
