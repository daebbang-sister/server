package com.daebbang.daebbangcore.domain.order.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.repository.OrderDetailsRepository;
import com.daebbang.daebbangcore.domain.order.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailsRepository orderDetailsRepository;

    @Override
    public OrderDetails getOrderDetailForReview(Long orderDetailId, Long userId) {
        OrderDetails orderDetail = orderDetailsRepository
            .findByIdAndUserIdAndStatus(orderDetailId, userId, OrderDetailStatus.NORMAL)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_DETAIL_NOT_FOUND));

        if (orderDetail.getOrder().getOrderStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_COMPLETED);
        }

        return orderDetail;
    }
}
