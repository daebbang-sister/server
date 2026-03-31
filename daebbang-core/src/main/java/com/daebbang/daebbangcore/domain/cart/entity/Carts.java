package com.daebbang.daebbangcore.domain.cart.entity;

import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Carts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_detail_id", nullable = false)
    private ProductDetails productDetail;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    private Carts(Users user, ProductDetails productDetail, Integer quantity) {
        this.user = user;
        this.productDetail = productDetail;
        this.quantity = quantity;
    }

    public static Carts create(Users user, ProductDetails productDetail, Integer quantity) {
        return Carts.builder()
            .user(user)
            .productDetail(productDetail)
            .quantity(quantity)
            .build();
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    public void updateProductDetail(ProductDetails productDetails) {
        this.productDetail = productDetails;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }
}
