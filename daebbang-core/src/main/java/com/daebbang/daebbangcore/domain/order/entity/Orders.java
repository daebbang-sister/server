package com.daebbang.daebbangcore.domain.order.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.util.Arrays;
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

    @Column(length = 50, nullable = false)
    private String receiver;

    @Column(length = 20, nullable = false)
    private String receiverPhone;

    @Column(length = 10, nullable = false)
    private String zipCode;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(length = 255, nullable = false)
    private String detailAddress;

    @Column(length = 100)
    private String orderNote;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @jakarta.persistence.OrderBy("id ASC")
    private final List<OrderDetails> orderList = new ArrayList<>();

    @Builder
    private Orders(Users user, String orderNumber, int usedPoint, int shippingFee,
        int totalOriginalAmount, int totalSellingAmount,
        String receiver, String receiverPhone, String zipCode, String address,
        String detailAddress, String orderNote) {
        this.user = user;
        this.orderNumber = orderNumber;
        this.orderStatus = OrderStatus.PENDING;
        this.usedPoint = usedPoint;
        this.shippingFee = shippingFee;
        this.totalOriginalAmount = totalOriginalAmount;
        this.totalSellingAmount = totalSellingAmount;
        this.paymentAmount = totalSellingAmount + shippingFee - usedPoint;
        this.receiver = receiver;
        this.receiverPhone = receiverPhone;
        this.zipCode = zipCode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.orderNote = orderNote;
    }

    public static Orders create(Users user, String orderNumber, int usedPoint, int shippingFee,
        int totalOriginalAmount, int totalSellingAmount,
        String receiver, String receiverPhone, String zipCode, String address,
        String detailAddress, String orderNote) {
        return Orders.builder()
            .user(user)
            .orderNumber(orderNumber)
            .usedPoint(usedPoint)
            .shippingFee(shippingFee)
            .totalOriginalAmount(totalOriginalAmount)
            .totalSellingAmount(totalSellingAmount)
            .receiver(receiver)
            .receiverPhone(receiverPhone)
            .zipCode(zipCode)
            .address(address)
            .detailAddress(detailAddress)
            .orderNote(orderNote)
            .build();
    }

    public void addDetail(OrderDetails detail) {
        orderList.add(detail);
        detail.assignOrder(this);
    }

    public void pay() {
        requireStatus(OrderStatus.PENDING);
        this.orderStatus = OrderStatus.PAID;
    }

    public void waitDeposit() {
        requireStatus(OrderStatus.PENDING);
        this.orderStatus = OrderStatus.WAITING_DEPOSIT;
    }

    public void prepareDelivery() {
        requireStatus(OrderStatus.PAID);
        this.orderStatus = OrderStatus.PREPARING_DELIVERY;
    }

    public void startDelivery() {
        requireStatus(OrderStatus.PREPARING_DELIVERY);
        this.orderStatus = OrderStatus.IN_DELIVERY;
    }

    public void delivered() {
        requireStatus(OrderStatus.IN_DELIVERY);
        this.orderStatus = OrderStatus.DELIVERED;
    }

    public void complete() {
        requireStatus(OrderStatus.DELIVERED);
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void cancel() {
        requireStatus(OrderStatus.PENDING, OrderStatus.WAITING_DEPOSIT, OrderStatus.PAID, OrderStatus.PREPARING_DELIVERY);
        this.orderStatus = OrderStatus.CANCELLED;
    }

    public void expire() {
        requireStatus(OrderStatus.PENDING, OrderStatus.WAITING_DEPOSIT);
        this.orderStatus = OrderStatus.EXPIRED;
    }

    public boolean isPending() {
        return this.orderStatus == OrderStatus.PENDING
            || this.orderStatus == OrderStatus.WAITING_DEPOSIT;
    }

    private void requireStatus(OrderStatus... allowed) {
        for (OrderStatus s : allowed) {
            if (this.orderStatus == s) return;
        }
        throw new IllegalStateException(
            "주문 상태 전환 불가. 현재: " + this.orderStatus + ", 허용: " + Arrays.toString(allowed));
    }
}
