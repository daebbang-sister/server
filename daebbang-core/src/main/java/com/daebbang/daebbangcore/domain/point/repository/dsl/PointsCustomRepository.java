package com.daebbang.daebbangcore.domain.point.repository.dsl;

import com.daebbang.daebbangcore.domain.point.entity.Points;
import java.util.Optional;

public interface PointsCustomRepository {

    /**
     * 적립금 변동용 비관적 락 조회. 락 타임아웃 3초가 실제 SELECT … FOR UPDATE 실행 전에
     * 적용되도록 EntityManager로 setHint → setLockMode 순서로 명시 호출한다.
     */
    Optional<Points> findByUserIdForUpdate(Long userId);
}
