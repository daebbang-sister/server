package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcore.domain.order.command.OrderCancelCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderConfirmCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderPartialCancelCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import com.daebbang.daebbangcore.domain.order.dto.OrderFullDetailResult;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.dto.OrderSummaryResult;
import java.time.LocalDateTime;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderPrepareResponse prepare(OrderPrepareCommand command);

    void confirm(OrderConfirmCommand command);

    void cancel(OrderCancelCommand command);

    void cancelPartial(OrderPartialCancelCommand command);

    /**
     * 사용자가 배송 완료된 주문을 구매 확정. 적립금 정책에 따라 PURCHASE 적립이 함께 일어난다.
     */
    void complete(Long userId, String orderNumber);

    Page<@NonNull OrderSummaryResult> getOrderList(Long userId, LocalDateTime start, LocalDateTime end,
                                          Pageable pageable);

    OrderFullDetailResult getOrderDetail(Long userId, String orderNumber);

    OrderStatusCountResult getOrderStatusCount(Long userId, LocalDateTime start, LocalDateTime end);
}
