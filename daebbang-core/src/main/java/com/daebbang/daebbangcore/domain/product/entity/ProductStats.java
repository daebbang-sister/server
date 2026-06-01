package com.daebbang.daebbangcore.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(
    name = "uq_product_stats_product",
    columnNames = {"product_id"}
))
public class ProductStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int viewCount;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int wishCount;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int salesCount;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int reviewCount;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private ProductStats(Products product) {
        this.product = product;
        this.viewCount = 0;
        this.wishCount = 0;
        this.salesCount = 0;
        this.reviewCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductStats create(Products product) {
        return new ProductStats(product);
    }

    public void update() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addViewCount(int delta) {
        this.viewCount = Math.max(0, this.viewCount + delta);
    }

    public void addWishCount(int delta) {
        this.wishCount = Math.max(0, this.wishCount + delta);
    }

    public void addSalesCount(int delta) {
        this.salesCount = Math.max(0, this.salesCount + delta);
    }

    public void addReviewCount(int delta) {
        this.reviewCount = Math.max(0, this.reviewCount + delta);
    }
}
