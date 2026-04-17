package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcore.domain.order.command.OrderConfirmCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;

public interface OrderService {

    OrderPrepareResponse prepare(OrderPrepareCommand command);

    void confirm(OrderConfirmCommand command);
}
