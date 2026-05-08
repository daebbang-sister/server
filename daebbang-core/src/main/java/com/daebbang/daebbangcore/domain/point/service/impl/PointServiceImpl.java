package com.daebbang.daebbangcore.domain.point.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.PointErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.ChangeType;
import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.Points;
import com.daebbang.daebbangcore.domain.point.entity.PolicyType;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.repository.PointPolicyRepository;
import com.daebbang.daebbangcore.domain.point.repository.PointsRepository;
import com.daebbang.daebbangcore.domain.point.repository.UserPointHistoryRepository;
import com.daebbang.daebbangcore.domain.point.service.PointService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointServiceImpl implements PointService {

    private static final int MIN_PAYMENT_AMOUNT_FOR_POINT_USE = 30_000;

    private final PointsRepository pointsRepository;
    private final PointPolicyRepository pointPolicyRepository;
    private final UserPointHistoryRepository userPointHistoryRepository;
    private final UsersRepository usersRepository;

    @Override
    public PointBalanceResult getBalance(Long userId) {
        return pointsRepository.findByUserIdAndDeletedAtIsNull(userId)
            .map(this::toBalance)
            .orElseGet(PointBalanceResult::zero);
    }

    @Override
    public Page<@NonNull UserPointHistory> getHistory(Long userId, Pageable pageable) {
        return userPointHistoryRepository.findPageByUserId(userId, pageable);
    }

    @Override
    public Optional<PointPolicy> findActiveByType(PolicyType policyType) {
        return pointPolicyRepository.findFirstByPolicyTypeAndIsActiveTrueAndDeletedAtIsNull(policyType);
    }

    @Override
    @Transactional
    public void awardSignupPoint(Long userId) {
        Points points = createIfMissing(userId);
        award(points, PolicyType.SIGNUP, ChangeType.EARN_SIGNUP, null, 0);
    }

    @Override
    @Transactional
    public void awardReviewPoint(Long userId, Long reviewId, boolean isPhotoReview) {
        Points points = loadForUpdate(userId);
        PolicyType type = isPhotoReview ? PolicyType.REVIEW_PHOTO : PolicyType.REVIEW_TEXT;
        award(points, type, ChangeType.EARN_REVIEW, reviewId, 0);
    }

    @Override
    @Transactional
    public void usePointForPayment(Long userId, Long orderId, int useAmount, int paymentEligibleAmount) {
        if (useAmount <= 0) {
            return;
        }
        if (paymentEligibleAmount < MIN_PAYMENT_AMOUNT_FOR_POINT_USE) {
            throw new BusinessException(PointErrorCode.POINT_USE_BELOW_MIN_ORDER);
        }
        Points points = loadForUpdate(userId);
        points.use(useAmount);

        userPointHistoryRepository.save(UserPointHistory.ofChange(
            points, ChangeType.USE_PAYMENT, orderId,
            useAmount, points.getCurrentAmount(), "결제 시 적립금 사용"
        ));
    }

    @Override
    @Transactional
    public void refundUsedPoint(Long userId, Long orderId, int amount) {
        if (amount <= 0) {
            return;
        }
        Points points = loadForUpdate(userId);
        points.refund(amount);

        userPointHistoryRepository.save(UserPointHistory.ofChange(
            points, ChangeType.REFUND_CANCEL, orderId,
            amount, points.getCurrentAmount(), "주문 취소에 따른 적립금 환원"
        ));
    }

    /**
     * 정책 기반 적립의 단일 처리 로직. 정책 미설정/비활성/계산값 0이면 조용히 종료.
     */
    private void award(Points points, PolicyType policyType, ChangeType changeType,
        Long referenceId, int rateBaseAmount) {

        PointPolicy policy = findActiveByType(policyType).orElse(null);
        if (policy == null) {
            log.info("[Point] 활성 정책 없음 - skip. userId={}, policyType={}",
                points.getUser().getId(), policyType);
            return;
        }

        int amount = policy.calculateEarnAmount(rateBaseAmount);
        if (amount <= 0) {
            log.info("[Point] 계산된 적립액 0 - skip. userId={}, policyType={}",
                points.getUser().getId(), policyType);
            return;
        }

        points.earn(amount);

        LocalDateTime expiredAt = policy.resolveExpiredAt(LocalDateTime.now());
        userPointHistoryRepository.save(UserPointHistory.ofEarn(
            points, policy, changeType, referenceId,
            amount, points.getCurrentAmount(), policy.getName(), expiredAt
        ));
    }

    /**
     * 회원가입 진입점 전용. 동일 userId 동시 진입 불가가 보장되는 경로에서만 호출되어야 race-free.
     */
    private Points createIfMissing(Long userId) {
        return pointsRepository.findByUserIdForUpdate(userId)
            .orElseGet(() -> {
                Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
                return pointsRepository.save(Points.create(user));
            });
    }

    /**
     * 회원가입 외 모든 변동 진입점에서 사용. row 부재는 회원가입 단계가 누락됐다는 의미이므로 예외.
     */
    private Points loadForUpdate(Long userId) {
        return pointsRepository.findByUserIdForUpdate(userId)
            .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND));
    }

    private PointBalanceResult toBalance(Points points) {
        return new PointBalanceResult(
            points.getCurrentAmount(),
            points.getTotalEarned(),
            points.getTotalUsed()
        );
    }
}
