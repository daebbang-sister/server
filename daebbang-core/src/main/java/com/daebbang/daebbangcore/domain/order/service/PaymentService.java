package com.daebbang.daebbangcore.domain.order.service;

import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.entity.Payment;

public interface PaymentService {

    void save(Payment payment);

    Payment findByOrder(Orders order);

    /**
     * 결제 취소 이력 기록 (전액 / 부분 취소 공통)
     * - PaymentCancels 생성 후 Payment.addCancel() 호출
     * - totalCancelAmount 갱신 및 PaymentStatus 변경 포함
     */
    void recordCancel(Payment payment, String cancelReason, int cancelAmount);
}
