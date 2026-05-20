package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {

    @Query("""
        SELECT od FROM OrderDetails od
        JOIN od.order o
        WHERE od.id = :id
          AND o.user.id = :userId
          AND od.status = :status
        """)
    Optional<OrderDetails> findByIdAndUserIdAndStatus(
        @Param("id") Long id,
        @Param("userId") Long userId,
        @Param("status") OrderDetailStatus status
    );

    /**
     * 클레임 생성 시 동시성 제어용. PESSIMISTIC_WRITE 락으로 동일 주문상세에 대한
     * 중복 클레임 신청을 직렬화한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT od FROM OrderDetails od
        JOIN od.order o
        WHERE od.id = :id
          AND o.user.id = :userId
          AND od.status = :status
        """)
    Optional<OrderDetails> findByIdAndUserIdAndStatusForUpdate(
        @Param("id") Long id,
        @Param("userId") Long userId,
        @Param("status") OrderDetailStatus status
    );
}
