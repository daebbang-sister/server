package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.domain.category.entity.Category;
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
public class ProductCategory extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Builder
    private ProductCategory(Category category, Products product) {
        this.category = category;
        this.product = product;
    }

    public static ProductCategory create(Category category, Products product) {
        return ProductCategory.builder()
                                .category(category)
                                .product(product)
                                .build();
    }
}
