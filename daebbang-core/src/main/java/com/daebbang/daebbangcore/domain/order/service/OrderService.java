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
import java.util.List;
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

    /**
     * 자동 확정 후보 주문 ID 목록 조회. 배치가 단건 처리 진입점으로 사용.
     */
    List<Long> findAutoCompletableOrderIds();

    /**
     * 시스템 트리거 자동 구매 확정. 회원 인증 검증 없이 호출되며, 단건 트랜잭션(REQUIRES_NEW).
     * 이미 다른 상태로 전환된 주문은 조용히 skip.
     */
    void autoComplete(Long orderId);

    Page<@NonNull OrderSummaryResult> getOrderList(Long userId, LocalDateTime start, LocalDateTime end,
                                          Pageable pageable);

    OrderFullDetailResult getOrderDetail(Long userId, String orderNumber);

    OrderStatusCountResult getOrderStatusCount(Long userId, LocalDateTime start, LocalDateTime end);
}
