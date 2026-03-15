package com.daebbang.daebbangcore.domain.product.dto;

import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import java.time.LocalDate;

public record ProductCardQueryResult(
    Long id,
    String categoryName,
    String productName,
    String mainImageUrl,
    String hoverImageUrl,
    Integer originalPrice,
    DiscountType discountType,
    Integer discountRate,
    LocalDate discountStartDate,
    LocalDate discountEndDate,
    ProductStatus productStatus
//    List<String> colors
) {

}
