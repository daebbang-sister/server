package com.daebbang.daebbangcore.domain.order.command;

import java.util.List;

public record OrderPrepareCommand(
    Long userId,
    List<OrderItemCommand> items,
    int usedPoint,
    String receiver,
    String receiverPhoneNumber,
    String zipCode,
    String address,
    String detailAddress,
    int shippingFee,
    boolean isAddToAddressBook,
    String addressAlias
) {}
