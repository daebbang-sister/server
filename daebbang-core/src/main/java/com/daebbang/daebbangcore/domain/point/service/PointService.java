package com.daebbang.daebbangcore.domain.point.service;

import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.PolicyType;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointService {

    PointBalanceResult getBalance(Long userId);

    Page<@NonNull UserPointHistory> getHistory(Long userId, Pageable pageable);

    /**
     * 활성 정책 조회. 관리자 화면이나 예상 적립금 표시 등에 사용.
     */
    Optional<PointPolicy> findActiveByType(PolicyType policyType);

    /**
     * 회원가입 적립. 활성 SIGNUP 정책이 없으면 적립 없이 정상 종료.
     */
    void awardSignupPoint(Long userId);

    /**
     * 리뷰 승인 시 적립. isPhotoReview에 따라 REVIEW_TEXT 또는 REVIEW_PHOTO 정책 적용.
     * 정책 미설정 시 적립 없이 정상 종료.
     */
    void awardReviewPoint(Long userId, Long reviewId, boolean isPhotoReview);

    /**
     * 결제 시 적립금 사용. 30,000원 이상 결제에서만 사용 가능.
     *
     * @param userId               회원 아이디
     * @param orderId              주문 아이디 (history reference_id)
     * @param useAmount            사용할 적립금 (양수)
     * @param paymentEligibleAmount 적립금 사용 자격 판단 기준 금액 (상품 + 배송비)
     */
    void usePointForPayment(Long userId, Long orderId, int useAmount, int paymentEligibleAmount);

    /**
     * 주문 취소·환불에 따라 사용한 적립금을 환원한다. 0 이하 amount는 무시.
     */
    void refundUsedPoint(Long userId, Long orderId, int amount);
}
