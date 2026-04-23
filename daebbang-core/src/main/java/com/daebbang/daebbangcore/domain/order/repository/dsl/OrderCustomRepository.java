package com.daebbang.daebbangcore.domain.order.repository.dsl;

import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderCustomRepository {

    /**
     * 기간 내 주문 목록 페이징 조회
     * - 1단계(ID 페이징) + 2단계(fetch join) 분리로 in-memory 페이징 방지
     */
    Page<Orders> findOrdersByUserIdAndDateRange(Long userId, LocalDateTime start,
                                                LocalDateTime end, Pageable pageable);

    /**
     * 주문 상세 조회 (orderList + productDetail + product fetch join)
     * - 취소 API / 상세 조회 API 공통 사용
     */
    Optional<Orders> findOrderDetailByOrderNumberAndUserId(String orderNumber, Long userId);

    /**
     * 기간 내 배송 현황 건수 (결제완료 / 배송준비중 / 배송중 / 배송완료)
     */
    OrderStatusCountResult countOrderStatusByUserIdAndDateRange(Long userId, LocalDateTime start,
                                                                LocalDateTime end);
}
