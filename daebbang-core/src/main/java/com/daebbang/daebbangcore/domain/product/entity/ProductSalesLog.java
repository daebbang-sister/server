package com.daebbang.daebbangcore.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "product_sales_log",
    uniqueConstraints = @UniqueConstraint(name = "uq_product_sales_log", columnNames = {"product_id", "sale_date"})
)
public class ProductSalesLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Column(nullable = false)
    private long salesCount;

    private ProductSalesLog(Products product, LocalDate saleDate, long salesCount) {
        this.product = product;
        this.saleDate = saleDate;
        this.salesCount = salesCount;
    }

    public static ProductSalesLog of(Products product, LocalDate saleDate, long salesCount) {
        return new ProductSalesLog(product, saleDate, salesCount);
    }
}
