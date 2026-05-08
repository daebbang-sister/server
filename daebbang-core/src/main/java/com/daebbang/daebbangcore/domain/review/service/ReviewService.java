package com.daebbang.daebbangcore.domain.review.service;

import com.daebbang.daebbangcore.domain.review.command.CreateReviewCommand;
import com.daebbang.daebbangcore.domain.review.command.UpdateReviewCommand;
import com.daebbang.daebbangcore.domain.review.dto.ReviewExpectedAmounts;
import com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import java.util.List;
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

    /**
     * 마이페이지 리뷰 목록 표시용. 활성 정책이 없으면 0 반환.
     */
    ReviewExpectedAmounts getReviewExpectedAmounts();

    /**
     * 자동 승인 대상이 되는 PENDING 리뷰 ID 목록을 조회. 스케줄러에서 단건 처리 진입점으로 사용.
     */
    List<Long> findPendingReviewIdsToApprove();

    /**
     * 단일 리뷰의 적립을 승인하고 적립금을 지급. 호출 단위로 새 트랜잭션이 열린다 (REQUIRES_NEW).
     * 한 건 실패가 다른 건 처리에 영향을 주지 않도록 분리되어 있다.
     */
    void approveReviewPoint(Long reviewId);
}
