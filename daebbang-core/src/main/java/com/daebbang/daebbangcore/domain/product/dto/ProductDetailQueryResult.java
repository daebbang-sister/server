package com.daebbang.daebbangcore.domain.product.dto;

import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import java.time.LocalDate;

public record ProductDetailQueryResult(
    Long id,
    String categoryName,
    String productName,
    String simpleDescription,
    String mainImageUrl,
    Integer originalPrice,
    DiscountType discountType,
    Integer discountRate,
    LocalDate discountStartDate,
    LocalDate discountEndDate,
    String descriptionHtml
) {
}
