package com.daebbang.daebbangcore.domain.product.entity;

import com.daebbang.daebbangcore.domain.audit.DefaultBase;
import com.daebbang.daebbangcore.infra.converter.DiscountTypeConverter;
import com.daebbang.daebbangcore.infra.converter.ProductStatusConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Products extends DefaultBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = ProductStatusConverter.class)
    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private ProductStatus productStatus;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer originalPrice;

    @Column
    private String simpleDescription;

    @Column(nullable = false)
    private String mainImageUrl;

    private String hoverImageUrl;

    @Convert(converter = DiscountTypeConverter.class)
    @Column(columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private DiscountType discountType;

    @Column(columnDefinition = "TINYINT UNSIGNED")
    private Integer discountRate;

    private LocalDate discountStartDate;

    private LocalDate discountEndDate;

    @Column(columnDefinition = "TEXT")
    private String descriptionHtml;

    @Builder
    private Products(ProductStatus status, String productName,
        Integer originalPrice,
        String simpleDescription, String mainImageUrl, String hoverImageUrl,
        DiscountType discountType, Integer discountRate, LocalDate discountStartDate, LocalDate discountEndDate) {
        this.productStatus = status;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.simpleDescription = simpleDescription;
        this.mainImageUrl = mainImageUrl;
        this.hoverImageUrl = hoverImageUrl;
        this.discountType = discountType;
        this.discountRate = discountRate;
        this.discountStartDate = discountStartDate;
        this.discountEndDate = discountEndDate;
    }

    public static Products create(
        ProductStatus status, String productName,
        Integer originalPrice,
        String simpleDescription, String mainImageUrl, String hoverImageUrl,
        DiscountType discountType, Integer discountRate,
        LocalDate discountStartDate, LocalDate discountEndDate) {
        return Products.builder()
                        .status(status)
                        .productName(productName)
                        .originalPrice(originalPrice)
                        .simpleDescription(simpleDescription)
                        .mainImageUrl(mainImageUrl)
                        .hoverImageUrl(hoverImageUrl)
                        .discountType(discountType)
                        .discountRate(discountRate)
                        .discountStartDate(discountStartDate)
                        .discountEndDate(discountEndDate)
                        .build();
    }

    public void deleteProduct() {
        this.delete();
    }
}
