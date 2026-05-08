package com.daebbang.daebbangcore.domain.point.repository;

import com.daebbang.daebbangcore.domain.point.entity.ChangeType;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.repository.dsl.UserPointHistoryCustomRepository;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPointHistoryRepository
    extends JpaRepository<@NonNull UserPointHistory, @NonNull Long>, UserPointHistoryCustomRepository {

    @Query("""
        SELECT COALESCE(SUM(h.changeAmount), 0)
        FROM UserPointHistory h
        WHERE h.changeType = :changeType
          AND h.referenceId = :referenceId
        """)
    Integer sumChangeAmountByChangeTypeAndReferenceId(
        @Param("changeType") ChangeType changeType,
        @Param("referenceId") Long referenceId
    );
}
