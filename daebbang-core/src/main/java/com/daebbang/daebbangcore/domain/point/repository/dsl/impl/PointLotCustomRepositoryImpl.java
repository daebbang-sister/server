package com.daebbang.daebbangcore.domain.point.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.point.entity.PointLot;
import com.daebbang.daebbangcore.domain.point.repository.dsl.PointLotCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PointLotCustomRepositoryImpl implements PointLotCustomRepository {

    private static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";
    private static final long LOCK_TIMEOUT_MS = 3_000L;

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<PointLot> findActiveLotsForUpdate(Long pointsId) {
        return em.createQuery("""
                SELECT l FROM PointLot l
                WHERE l.points.id = :pointsId
                  AND l.remainingAmount > 0
                ORDER BY l.createdAt ASC, l.id ASC
                """, PointLot.class)
            .setParameter("pointsId", pointsId)
            .setHint(LOCK_TIMEOUT_HINT, LOCK_TIMEOUT_MS)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultList();
    }

    @Override
    public List<PointLot> findExpirableLotsForUpdate(Long pointsId, LocalDateTime now) {
        return em.createQuery("""
                SELECT l FROM PointLot l
                WHERE l.points.id = :pointsId
                  AND l.remainingAmount > 0
                  AND l.expiredAt IS NOT NULL
                  AND l.expiredAt <= :now
                ORDER BY l.createdAt ASC, l.id ASC
                """, PointLot.class)
            .setParameter("pointsId", pointsId)
            .setParameter("now", now)
            .setHint(LOCK_TIMEOUT_HINT, LOCK_TIMEOUT_MS)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultList();
    }

    @Override
    public List<Long> findPointsIdsWithExpirableLots(LocalDateTime now) {
        return em.createQuery("""
                SELECT DISTINCT l.points.id FROM PointLot l
                WHERE l.remainingAmount > 0
                  AND l.expiredAt IS NOT NULL
                  AND l.expiredAt <= :now
                """, Long.class)
            .setParameter("now", now)
            .getResultList();
    }
}
