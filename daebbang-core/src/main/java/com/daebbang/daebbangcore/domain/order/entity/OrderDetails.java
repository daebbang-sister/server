package com.daebbang.daebbangcore.domain.order.entity;

import com.daebbang.daebbangcore.domain.audit.CreatedBase;
import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetails extends CreatedBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetails productDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer originalPrice;

    @Column(nullable = false)
    private Integer discountRate;

    @Column(nullable = false)
    private Integer discountPrice;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OrderDetailStatus status;

    @Builder
    private OrderDetails(ProductDetails productDetail, Integer quantity,
        Integer originalPrice, Integer discountRate, Integer discountPrice) {
        this.productDetail = productDetail;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountPrice = discountPrice;
        this.status = OrderDetailStatus.NORMAL;
    }

    public static OrderDetails create(ProductDetails productDetail, int quantity,
        int originalPrice, int discountRate) {
        return OrderDetails.builder()
            .productDetail(productDetail)
            .quantity(quantity)
            .originalPrice(originalPrice)
            .discountRate(discountRate)
            .discountPrice((int) (originalPrice * (1 - discountRate / 100.0)) * quantity)
            .build();
    }

    void assignOrder(Orders order) {
        this.order = order;
    }

    public void cancelRequest() {
        this.status = OrderDetailStatus.CANCEL_REQUESTED;
    }

    public void cancel() {
        this.status = OrderDetailStatus.CANCELLED;
    }

    public void refundRequest() {
        this.status = OrderDetailStatus.REFUND_REQUESTED;
    }

    public void returned() {
        this.status = OrderDetailStatus.RETURNED;
    }
}
