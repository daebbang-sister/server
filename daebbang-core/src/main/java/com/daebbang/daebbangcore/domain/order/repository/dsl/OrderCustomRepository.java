package com.daebbang.daebbangcore.domain.order.repository.dsl;

import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderCustomRepository {

    Page<@NonNull Orders> findOrdersByUserIdAndDateRange(Long userId, LocalDateTime start,
                                                LocalDateTime end, Pageable pageable);

    Optional<Orders> findOrderDetailByOrderNumberAndUserId(String orderNumber, Long userId);

    OrderStatusCountResult countOrderStatusByUserIdAndDateRange(Long userId, LocalDateTime start,
                                                                LocalDateTime end);
}
