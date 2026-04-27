package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
