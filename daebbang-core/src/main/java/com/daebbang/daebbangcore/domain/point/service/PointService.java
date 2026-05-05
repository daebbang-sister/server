package com.daebbang.daebbangcore.domain.point.service;

import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointService {

    PointBalanceResult getBalance(Long userId);

    Page<@NonNull UserPointHistory> getHistory(Long userId, Pageable pageable);
}
