package com.daebbang.daebbangcore.domain.point.repository.dsl;

import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserPointHistoryCustomRepository {

    Page<@NonNull UserPointHistory> findPageByUserId(Long userId, Pageable pageable);
}
