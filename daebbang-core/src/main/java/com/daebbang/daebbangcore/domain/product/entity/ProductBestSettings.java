package com.daebbang.daebbangcore.domain.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_best_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductBestSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer maxPeriodDays;

    @Column(nullable = false)
    private Integer defaultPeriodDays;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void update(int maxPeriodDays, int defaultPeriodDays) {
        this.maxPeriodDays = maxPeriodDays;
        this.defaultPeriodDays = defaultPeriodDays;
        this.updatedAt = LocalDateTime.now();
    }
}
