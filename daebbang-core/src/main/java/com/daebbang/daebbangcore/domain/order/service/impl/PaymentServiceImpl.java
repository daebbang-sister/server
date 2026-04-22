package com.daebbang.daebbangcore.domain.order.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.entity.Payment;
import com.daebbang.daebbangcore.domain.order.entity.PaymentCancels;
import com.daebbang.daebbangcore.domain.order.repository.PaymentRepository;
import com.daebbang.daebbangcore.domain.order.service.PaymentService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void save(Payment payment) {
        paymentRepository.save(payment);
    }

    @Override
    public Payment findByOrder(Orders order) {
        return paymentRepository.findByOrder(order)
            .orElseThrow(() -> new BusinessException(UserErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 트랜잭션 내에서 호출되므로 별도 save 불필요.
     * Payment는 이미 영속 상태 → addCancel() 후 dirty checking으로 자동 반영.
     */
    @Override
    @Transactional
    public void recordCancel(Payment payment, String cancelReason, int cancelAmount) {
        PaymentCancels cancel = PaymentCancels.create(cancelReason, cancelAmount, LocalDateTime.now());
        payment.addCancel(cancel);
    }
}
