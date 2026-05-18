package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.repository.dsl.OrderCustomRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<@NonNull Orders, @NonNull Long>,
    OrderCustomRepository {

    boolean existsByOrderNumber(String orderNumber);

    @Query("""
        SELECT o.id FROM Orders o
        WHERE o.orderStatus = :status
          AND o.updatedAt <= :threshold
          AND o.deletedAt IS NULL
        ORDER BY o.id ASC
        """)
    List<Long> findOrderIdsByStatusAndUpdatedBefore(
        @Param("status") OrderStatus status,
        @Param("threshold") LocalDateTime threshold
    );
}
