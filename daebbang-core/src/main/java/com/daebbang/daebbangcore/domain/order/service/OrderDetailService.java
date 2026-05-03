package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;

public interface OrderDetailService {

    OrderDetails getOrderDetailForReview(Long orderDetailId, Long userId);
}
