package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDetails extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(nullable = false, length = 50)
    private String colorCode;

    @Column(nullable = false)
    private Integer stock;

    @Builder
    private ProductDetails(Products product, String color, String size, String colorCode, Integer stock) {
        this.product = product;
        this.color = color;
        this.size = size;
        this.colorCode = colorCode;
        this.stock = getDefaultValue(stock);
    }

    public static ProductDetails create(Products product, String color, String colorCode, String size, Integer stock) {
        return ProductDetails.builder()
                            .product(product)
                            .color(color)
                            .colorCode(colorCode)
                            .size(size)
                            .stock(stock)
                            .build();
    }

    private Integer getDefaultValue(Integer stock) {
        return Objects.isNull(stock) ? 0 : stock;
    }
}
