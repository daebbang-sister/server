package com.daebbang.daebbangcore.domain.point.service;

import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.PolicyType;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointService {

    PointBalanceResult getBalance(Long userId);

    Page<@NonNull UserPointHistory> getHistory(Long userId, Pageable pageable);

    Optional<PointPolicy> findActiveByType(PolicyType policyType);

    void awardSignupPoint(Long userId);

    void awardReviewPoint(Long userId, Long reviewId, boolean isPhotoReview);

    /**
     * 구매 확정 시점에 PURCHASE 정책 기반 적립. RATE 정책일 때 결제 금액 기준으로 계산.
     */
    void awardPurchasePoint(Long userId, Long orderId, int paymentAmount);

    /**
     * 결제 금액 기준 예상 적립금 (PURCHASE 정책). 정책 미설정이면 0.
     */
    int calculateExpectedPurchasePoint(int paymentAmount);

    /**
     * 주문에 대해 이미 지급된 PURCHASE 적립금. 구매 확정 전이면 0.
     */
    int findEarnedPointByOrder(Long orderId);

    void usePointForPayment(Long userId, Long orderId, int useAmount, int paymentEligibleAmount);

    void refundUsedPoint(Long userId, Long orderId, int amount);

    /**
     * 만료 후보 회원 ID 목록 조회. 스케줄러가 회원별로 트랜잭션을 분리해 처리하기 위한 진입점.
     */
    List<Long> findUserIdsWithExpirablePoints();

    /**
     * 단일 회원의 만료 lot을 처리 (REQUIRES_NEW). 한 회원 처리 실패가 다른 회원 처리에 영향 X.
     */
    void expirePointsOfUser(Long userId);
}
