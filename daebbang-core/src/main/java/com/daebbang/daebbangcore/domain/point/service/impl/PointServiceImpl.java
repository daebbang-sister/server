package com.daebbang.daebbangcore.domain.point.service.impl;

import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.Points;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.repository.PointsRepository;
import com.daebbang.daebbangcore.domain.point.repository.UserPointHistoryRepository;
import com.daebbang.daebbangcore.domain.point.service.PointService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointServiceImpl implements PointService {

    private final PointsRepository pointsRepository;
    private final UserPointHistoryRepository userPointHistoryRepository;

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

    private PointBalanceResult toBalance(Points points) {
        return new PointBalanceResult(
            points.getCurrentAmount(),
            points.getTotalEarned(),
            points.getTotalUsed()
        );
    }
}
