package com.daebbang.daebbangcore.domain.point.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.PointErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.ChangeType;
import com.daebbang.daebbangcore.domain.point.entity.PointLot;
import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.Points;
import com.daebbang.daebbangcore.domain.point.entity.PolicyType;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.repository.PointLotRepository;
import com.daebbang.daebbangcore.domain.point.repository.PointPolicyRepository;
import com.daebbang.daebbangcore.domain.point.repository.PointsRepository;
import com.daebbang.daebbangcore.domain.point.repository.UserPointHistoryRepository;
import com.daebbang.daebbangcore.domain.point.service.PointService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.repository.UsersRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final PointLotRepository pointLotRepository;
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
    public void awardPurchasePoint(Long userId, Long orderId, int paymentAmount) {
        Points points = loadForUpdate(userId);
        award(points, PolicyType.PURCHASE, ChangeType.EARN_PURCHASE, orderId, paymentAmount);
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
        if (points.getCurrentAmount() < useAmount) {
            throw new BusinessException(PointErrorCode.POINT_INSUFFICIENT_BALANCE);
        }
        consumeFifo(points, useAmount);
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

        // 환불은 새 무기한 lot으로 복원 (원본 lot 추적은 비용 대비 가치 낮아 단순화)
        UserPointHistory history = userPointHistoryRepository.save(UserPointHistory.ofChange(
            points, ChangeType.REFUND_CANCEL, orderId,
            amount, points.getCurrentAmount(), "주문 취소에 따른 적립금 환원"
        ));
        pointLotRepository.save(PointLot.create(points, history, amount, null));
    }

    @Override
    public int calculateExpectedPurchasePoint(int paymentAmount) {
        return findActiveByType(PolicyType.PURCHASE)
            .map(p -> p.calculateEarnAmount(paymentAmount))
            .orElse(0);
    }

    @Override
    public int findEarnedPointByOrder(Long orderId) {
        Integer sum = userPointHistoryRepository.sumChangeAmountByChangeTypeAndReferenceId(
            ChangeType.EARN_PURCHASE, orderId);
        return sum != null ? sum : 0;
    }

    @Override
    public List<Long> findUserIdsWithExpirablePoints() {
        // pointsId → userId 변환은 만료 처리 메서드에서 처리. 여기선 pointsId 목록만 반환.
        return pointLotRepository.findPointsIdsWithExpirableLots(LocalDateTime.now());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expirePointsOfUser(Long pointsId) {
        Points points = pointsRepository.findById(pointsId)
            .orElseThrow(() -> new BusinessException(PointErrorCode.POINT_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        List<PointLot> expirable = pointLotRepository.findExpirableLotsForUpdate(pointsId, now);
        if (expirable.isEmpty()) {
            return;
        }

        int totalExpired = 0;
        for (PointLot lot : expirable) {
            totalExpired += lot.consume(lot.getRemainingAmount());
        }
        if (totalExpired <= 0) {
            return;
        }

        int actuallyExpired = points.expire(totalExpired);
        userPointHistoryRepository.save(UserPointHistory.ofChange(
            points, ChangeType.EXPIRE, null,
            actuallyExpired, points.getCurrentAmount(), "유효기간 만료에 따른 적립금 소멸"
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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = policy.resolveExpiredAt(now);
        UserPointHistory history = userPointHistoryRepository.save(UserPointHistory.ofEarn(
            points, policy, changeType, referenceId,
            amount, points.getCurrentAmount(), policy.getName(), expiredAt
        ));
        pointLotRepository.save(PointLot.create(points, history, amount, expiredAt));
    }

    /**
     * FIFO로 활성 lot에서 amount만큼 차감. 호출자가 잔액 충분성을 미리 검증해야 한다.
     */
    private void consumeFifo(Points points, int amount) {
        List<PointLot> activeLots = pointLotRepository.findActiveLotsForUpdate(points.getId());
        int remaining = amount;
        for (PointLot lot : activeLots) {
            if (remaining <= 0) break;
            remaining -= lot.consume(remaining);
        }
        if (remaining > 0) {
            // points.currentAmount 와 lots 합이 일치하지 않는 데이터 부정합. 비즈니스 가드.
            throw new BusinessException(PointErrorCode.POINT_INSUFFICIENT_BALANCE);
        }
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
