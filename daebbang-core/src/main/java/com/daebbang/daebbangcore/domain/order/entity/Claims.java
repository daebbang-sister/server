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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Claims extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetails orderDetail;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ClaimStatus claimStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private ReasonType reasonType;

    @Column(length = 300)
    private String reasonDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer refundAmount;

    @Column(nullable = false)
    private Integer refundPoint;

    @Column(length = 50, nullable = false)
    private String pickupReceiver;

    @Column(length = 20, nullable = false)
    private String pickupPhone;

    @Column(length = 10, nullable = false)
    private String pickupZipCode;

    @Column(nullable = false)
    private String pickupAddress;

    @Column
    private String pickupDetailAddress;

    @Column
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimImage> images = new ArrayList<>();

    @Builder
    private Claims(OrderDetails orderDetail, ClaimType claimType, ReasonType reasonType,
        String reasonDetail, Integer quantity, Integer refundAmount, Integer refundPoint,
        String pickupReceiver, String pickupPhone, String pickupZipCode,
        String pickupAddress, String pickupDetailAddress) {
        this.orderDetail = orderDetail;
        this.claimType = claimType;
        this.claimStatus = ClaimStatus.REQUESTED;
        this.reasonType = reasonType;
        this.reasonDetail = reasonDetail;
        this.quantity = quantity;
        this.refundAmount = refundAmount;
        this.refundPoint = refundPoint;
        this.pickupReceiver = pickupReceiver;
        this.pickupPhone = pickupPhone;
        this.pickupZipCode = pickupZipCode;
        this.pickupAddress = pickupAddress;
        this.pickupDetailAddress = pickupDetailAddress;
        update();
    }

    public static Claims create(OrderDetails orderDetail, ClaimType claimType,
        ReasonType reasonType, String reasonDetail, int quantity,
        int refundAmount, int refundPoint,
        String pickupReceiver, String pickupPhone, String pickupZipCode,
        String pickupAddress, String pickupDetailAddress) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("클레임 수량은 1 이상이어야 합니다. quantity=" + quantity);
        }
        if (refundAmount < 0) {
            throw new IllegalArgumentException("환불 금액은 0 이상이어야 합니다. refundAmount=" + refundAmount);
        }
        if (refundPoint < 0) {
            throw new IllegalArgumentException("환불 포인트는 0 이상이어야 합니다. refundPoint=" + refundPoint);
        }
        return Claims.builder()
            .orderDetail(orderDetail)
            .claimType(claimType)
            .reasonType(reasonType)
            .reasonDetail(reasonDetail)
            .quantity(quantity)
            .refundAmount(refundAmount)
            .refundPoint(refundPoint)
            .pickupReceiver(pickupReceiver)
            .pickupPhone(pickupPhone)
            .pickupZipCode(pickupZipCode)
            .pickupAddress(pickupAddress)
            .pickupDetailAddress(pickupDetailAddress)
            .build();
    }

    public void addImage(ClaimImage image) {
        if (image.getClaim() != this) {
            throw new IllegalArgumentException("ClaimImage의 claim 참조가 현재 엔티티와 일치하지 않습니다.");
        }
        this.images.add(image);
    }

    public void complete() {
        requireStatus(ClaimStatus.REQUESTED);
        this.claimStatus = ClaimStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        update();
    }

    public void reject() {
        requireStatus(ClaimStatus.REQUESTED);
        this.claimStatus = ClaimStatus.REJECTED;
        update();
    }

    private void requireStatus(ClaimStatus... allowed) {
        for (ClaimStatus s : allowed) {
            if (this.claimStatus == s) return;
        }
        throw new IllegalStateException(
            "클레임 상태 전환 불가. 현재: " + this.claimStatus + ", 허용: " + java.util.Arrays.toString(allowed));
    }
}
