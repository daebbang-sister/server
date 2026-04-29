package com.daebbang.daebbangcore.domain.order.session;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSession {

    private String orderNumber;
    private Long userId;
    private List<OrderSessionItem> items;
    private int usedPoint;
    private int shippingFee;
    private int totalOriginalAmount;
    private int totalSellingAmount;
    private int paymentAmount;
    private String receiver;
    private String receiverPhoneNumber;
    private String zipCode;
    private String address;
    private String detailAddress;
    private String orderNote;
}
