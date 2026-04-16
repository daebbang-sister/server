package com.daebbang.daebbangcore.domain.order.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.domain.user.entity.Users;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private Users user;

    @Column(length = 19, nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Integer usedPoint;

    @Column(nullable = false)
    private Integer shippingFee;

    @Column(nullable = false)
    private Integer totalOriginalAmount;

    @Column(nullable = false)
    private Integer totalSellingAmount;

    @Column(nullable = false)
    private Integer paymentAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<OrderDetails> orderList = new ArrayList<>();

    @Builder
    private Orders(Users user, String orderNumber, int usedPoint, int shippingFee,
        int totalOriginalAmount, int totalSellingAmount) {
        this.user = user;
        this.orderNumber = orderNumber;
        this.orderStatus = OrderStatus.PENDING;
        this.usedPoint = usedPoint;
        this.shippingFee = shippingFee;
        this.totalOriginalAmount = totalOriginalAmount;
        this.totalSellingAmount = totalSellingAmount;
        this.paymentAmount = totalSellingAmount + shippingFee - usedPoint;
    }

    public static Orders create(Users user, String orderNumber, int usedPoint, int shippingFee,
        int totalOriginalAmount, int totalSellingAmount) {
        return Orders.builder()
            .user(user)
            .orderNumber(orderNumber)
            .usedPoint(usedPoint)
            .shippingFee(shippingFee)
            .totalOriginalAmount(totalOriginalAmount)
            .totalSellingAmount(totalSellingAmount)
            .build();
    }

    public void addDetail(OrderDetails detail) {
        orderList.add(detail);
        detail.assignOrder(this);
    }

    public void pay() {
        this.orderStatus = OrderStatus.PAID;
    }

    public void waitDeposit() {
        this.orderStatus = OrderStatus.WAITING_DEPOSIT;
    }

    public void startDelivery() {
        this.orderStatus = OrderStatus.IN_DELIVERY;
    }

    public void delivered() {
        this.orderStatus = OrderStatus.DELIVERED;
    }

    public void complete() {
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void expire() {
        this.orderStatus = OrderStatus.EXPIRED;
    }

    public boolean isPending() {
        return this.orderStatus == OrderStatus.PENDING
            || this.orderStatus == OrderStatus.WAITING_DEPOSIT;
    }
}
