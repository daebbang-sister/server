package com.daebbang.daebbangapi.domain.wish.dto.response;

import com.daebbang.daebbangcore.domain.product.util.DiscountCalculator;
import com.daebbang.daebbangcore.domain.product.util.DiscountResult;
import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;

public record WishListInfo(
    Long wishListId,
    Long productId,
    String mainImageUrl,
    String productName,
    Integer originalPrice,
    Integer discountRate,
    Integer sellingPrice
) {

    public static WishListInfo from(WishListQueryResult result) {
        DiscountResult discount = DiscountCalculator.calculate(
            result.originalPrice(),
            result.discountType(),
            result.discountRate(),
            result.discountStartDate(),
            result.discountEndDate()
        );

        return new WishListInfo(
            result.wishListId(),
            result.productId(),
            result.mainImageUrl(),
            result.productName(),
            result.originalPrice(),
            discount.discountRate(),
            discount.sellingPrice()
        );
    }
}
