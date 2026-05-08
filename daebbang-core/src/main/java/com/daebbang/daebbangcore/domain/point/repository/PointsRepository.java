package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.Points;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends JpaRepository<@NonNull Points, @NonNull Long> {

    Optional<Points> findByUserIdAndDeletedAtIsNull(Long userId);

    /**
     * 적립금 잔액 변동 작업용 비관적 락 조회. 동일 사용자에 대한 동시 요청은 직렬화되어
     * 손실 업데이트(lost update)를 방지한다. 3초 내에 락을 잡지 못하면 LockTimeoutException 발생.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    @Query("""
        SELECT p FROM Points p
        WHERE p.user.id = :userId
          AND p.deletedAt IS NULL
        """)
    Optional<Points> findByUserIdForUpdate(@Param("userId") Long userId);
}
