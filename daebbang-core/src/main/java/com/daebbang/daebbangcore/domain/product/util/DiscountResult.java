package com.daebbang.daebbangcore.domain.product.util;

public record DiscountResult(
    Integer sellingPrice,
    Integer discountRate
) {
    public static DiscountResult noDiscount(Integer originalPrice) {
        return new DiscountResult(originalPrice, null);
    }

    public static DiscountResult discounted(Integer sellingPrice, Integer discountRate) {
        return new DiscountResult(sellingPrice, discountRate);
    }
}
