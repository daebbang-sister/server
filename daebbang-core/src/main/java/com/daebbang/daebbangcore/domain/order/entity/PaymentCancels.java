package com.daebbang.daebbangcore.domain.order.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCancels extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(length = 200, nullable = false)
    private String cancelReason;

    @Column(nullable = false)
    private Integer cancelAmount;

    @Column(nullable = false)
    private LocalDateTime cancelledAt;

    @Builder
    private PaymentCancels(String cancelReason, Integer cancelAmount, LocalDateTime cancelledAt) {
        this.cancelReason = cancelReason;
        this.cancelAmount = cancelAmount;
        this.cancelledAt = cancelledAt;
    }

    public static PaymentCancels create(String cancelReason, int cancelAmount, LocalDateTime cancelledAt) {
        return PaymentCancels.builder()
            .cancelReason(cancelReason)
            .cancelAmount(cancelAmount)
            .cancelledAt(cancelledAt)
            .build();
    }

    void assignPayment(Payment payment) {
        this.payment = payment;
    }
}
