package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcore.domain.audit.DeleteBase;
import com.daebbang.daebbangcore.infra.converter.ImageTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
public class ProductImage extends DeleteBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Convert(converter = ImageTypeConverter.class)
    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private ImageType imageType;

    @Column(nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private Short imageOrder;

    @Builder
    private ProductImage(Products product, ImageType imageType, String imageUrl, Short imageOrder) {
        this.product = product;
        this.imageType = imageType;
        this.imageUrl = imageUrl;
        this.imageOrder = imageOrder;
    }

    public static ProductImage create(Products product, ImageType imageType, String imageUrl, Short imageOrder) {
        return ProductImage.builder()
                            .product(product)
                            .imageType(imageType)
                            .imageUrl(imageUrl)
                            .imageOrder(imageOrder)
                            .build();
    }
}
