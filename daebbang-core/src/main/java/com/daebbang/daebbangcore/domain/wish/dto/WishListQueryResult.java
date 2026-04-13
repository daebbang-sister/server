package com.daebbang.daebbangcore.domain.wish.dto;

import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import java.time.LocalDate;

public record WishListQueryResult(
    Long wishListId,
    Long productId,
    String mainImageUrl,
    String productName,
    Integer originalPrice,
    DiscountType discountType,
    Integer discountRate,
    LocalDate discountStartDate,
    LocalDate discountEndDate
) {

}
