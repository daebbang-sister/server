package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.ClaimStatus;
import com.daebbang.daebbangcore.domain.order.entity.Claims;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claims, Long> {

    @Query("""
        SELECT c FROM Claims c
        JOIN c.orderDetail od
        JOIN od.order o
        WHERE od.id = :orderDetailId
          AND o.user.id = :userId
          AND c.claimStatus = :status
          AND c.deletedAt IS NULL
        """)
    Optional<Claims> findActiveByOrderDetailIdAndUserId(
        @Param("orderDetailId") Long orderDetailId,
        @Param("userId") Long userId,
        @Param("status") ClaimStatus status
    );

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Claims c
        WHERE c.orderDetail.id = :orderDetailId
          AND c.claimStatus = :status
          AND c.deletedAt IS NULL
        """)
    boolean existsByOrderDetailIdAndStatus(
        @Param("orderDetailId") Long orderDetailId,
        @Param("status") ClaimStatus status
    );
}
