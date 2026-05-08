package com.daebbang.daebbangcore.domain.point.repository.dsl;

import com.daebbang.daebbangcore.domain.point.entity.PointLot;
import java.time.LocalDateTime;
import java.util.List;

public interface PointLotCustomRepository {

    /**
     * 회원의 활성 lot을 FIFO 순서(생성일 오름차순)로 조회. 비관적 락(FOR UPDATE)을
     * 잡아 USE 시 동시 차감 충돌을 막는다. 락 timeout 3초.
     */
    List<PointLot> findActiveLotsForUpdate(Long pointsId);

    /**
     * 만료 처리 대상(만료일이 지났고 잔여가 남은) lot 조회. 비관적 락 포함.
     */
    List<PointLot> findExpirableLotsForUpdate(Long pointsId, LocalDateTime now);

    /**
     * 만료 배치가 처리할 모든 user의 만료 후보 points_id 목록을 distinct 조회.
     * (락 없이 후보만 식별. 실제 만료 작업은 user별로 트랜잭션 분리해 락 잡음)
     */
    List<Long> findPointsIdsWithExpirableLots(LocalDateTime now);
}
