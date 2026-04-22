package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.repository.dsl.OrderCustomRepository;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<@NonNull Orders, @NonNull Long>,
    OrderCustomRepository {

    boolean existsByOrderNumber(String orderNumber);
}
