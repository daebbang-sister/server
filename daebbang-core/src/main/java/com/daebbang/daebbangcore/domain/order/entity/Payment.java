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

    @Column
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

    @Column
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<PaymentCancels> cancels = new ArrayList<>();

    @Builder
    private Payment(Orders order, String paymentKey, String currency,
        String method, Integer totalAmount, PaymentStatus status,
        LocalDateTime requestedAt, LocalDateTime approvedAt) {
        this.order = order;
        this.status = status != null ? status : PaymentStatus.DONE;
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

    public static Payment createForBankTransfer(Orders order, int totalAmount) {
        return Payment.builder()
            .order(order)
            .currency("KRW")
            .method("BANK_TRANSFER")
            .totalAmount(totalAmount)
            .status(PaymentStatus.WAITING_DEPOSIT)
            .requestedAt(LocalDateTime.now())
            .build();
    }

    public void confirmDeposit(LocalDateTime approvedAt) {
        if (this.status != PaymentStatus.WAITING_DEPOSIT) {
            throw new IllegalStateException("입금 대기 상태가 아닙니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.DONE;
        this.approvedAt = approvedAt;
    }

    public void cancelBankTransfer() {
        if (this.status != PaymentStatus.WAITING_DEPOSIT) {
            throw new IllegalStateException("입금 대기 상태가 아닙니다. 현재: " + this.status);
        }
        this.status = PaymentStatus.CANCELLED;
    }

    public boolean isBankTransfer() {
        return "BANK_TRANSFER".equals(this.method);
    }

    public void addCancel(PaymentCancels cancel) {
        int newCancelTotal = this.totalCancelAmount + cancel.getCancelAmount();
        if (newCancelTotal > this.totalAmount) {
            throw new IllegalStateException("취소 금액 합계가 결제 금액을 초과할 수 없습니다.");
        }
        cancels.add(cancel);
        cancel.assignPayment(this);
        this.totalCancelAmount = newCancelTotal;
        this.status = (this.totalCancelAmount == this.totalAmount)
            ? PaymentStatus.CANCELLED
            : PaymentStatus.PARTIAL_CANCELLED;
    }
}
