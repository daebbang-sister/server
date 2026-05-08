package com.daebbang.daebbangcore.domain.point.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.point.entity.Points;
import com.daebbang.daebbangcore.domain.point.repository.dsl.PointsCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PointsCustomRepositoryImpl implements PointsCustomRepository {

    private static final String LOCK_TIMEOUT_HINT = "jakarta.persistence.lock.timeout";
    private static final long LOCK_TIMEOUT_MS = 3_000L;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Points> findByUserIdForUpdate(Long userId) {
        // setHint(lock.timeout) 을 setLockMode 보다 먼저 호출해야 follow-on locking 이슈를
        // 피해 SELECT ... FOR UPDATE 단계까지 타임아웃이 적용된다 (Spring Data JPA #4244 회피).
        List<Points> result = em.createQuery(
                """
                SELECT p FROM Points p
                WHERE p.user.id = :userId
                  AND p.deletedAt IS NULL
                """, Points.class)
            .setParameter("userId", userId)
            .setHint(LOCK_TIMEOUT_HINT, LOCK_TIMEOUT_MS)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultList();
        return result.stream().findFirst();
    }
}
