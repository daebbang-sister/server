package com.daebbang.daebbangcore.domain.order.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Orders order;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private String paymentKey;

    @Column(length = 20, nullable = false)
    private String currency;

    @Column(length = 20, nullable = false)
    private String method;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private Integer totalCancelAmount;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column(nullable = false)
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<PaymentCancels> cancels = new ArrayList<>();

    @Builder
    private Payment(Orders order, String paymentKey, String currency,
        String method, Integer totalAmount,
        LocalDateTime requestedAt, LocalDateTime approvedAt) {
        this.order = order;
        this.status = PaymentStatus.DONE;
        this.paymentKey = paymentKey;
        this.currency = currency;
        this.method = method;
        this.totalAmount = totalAmount;
        this.totalCancelAmount = 0;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }

    public static Payment create(Orders order, String paymentKey, String currency,
        String method, int totalAmount,
        LocalDateTime requestedAt, LocalDateTime approvedAt) {
        return Payment.builder()
            .order(order)
            .paymentKey(paymentKey)
            .currency(currency)
            .method(method)
            .totalAmount(totalAmount)
            .requestedAt(requestedAt)
            .approvedAt(approvedAt)
            .build();
    }

    public void addCancel(PaymentCancels cancel) {
        cancels.add(cancel);
        cancel.assignPayment(this);
        this.totalCancelAmount += cancel.getCancelAmount();
        this.status = this.totalCancelAmount >= this.totalAmount
            ? PaymentStatus.CANCELLED
            : PaymentStatus.PARTIAL_CANCELLED;
    }
}
