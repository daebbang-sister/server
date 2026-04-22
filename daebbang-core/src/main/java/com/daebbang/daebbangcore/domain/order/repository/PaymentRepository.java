package com.daebbang.daebbangcore.domain.order.repository;

import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.entity.Payment;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<@NonNull Payment, @NonNull Long> {

    Optional<Payment> findByOrder(Orders order);
}
