package com.daebbang.daebbangcore.infra.toss.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TossPaymentResponse {

    private String paymentKey;
    private String orderId;

    private String status;

    /** 카드 | 가상계좌 | 간편결제 | 계좌이체 등 */
    private String method;

    private String currency;

    private int totalAmount;

    private String requestedAt;

    private String approvedAt;

    public boolean isVirtualAccount() {
        return "가상계좌".equals(this.method);
    }
}
