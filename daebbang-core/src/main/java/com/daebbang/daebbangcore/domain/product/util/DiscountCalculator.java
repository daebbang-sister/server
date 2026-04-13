package com.daebbang.daebbangcore.domain.product.util;

import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import java.time.LocalDate;
import java.util.Objects;

public class DiscountCalculator {

    public static DiscountResult calculate(
        Integer originalPrice,
        DiscountType discountType,
        Integer discountRate,
        LocalDate discountStartDate,
        LocalDate discountEndDate
    ) {
        if (Objects.equals(discountType, DiscountType.NONE) || Objects.isNull(discountRate)) {
            return DiscountResult.noDiscount(originalPrice);
        }

        if (Objects.equals(discountType, DiscountType.PERIOD)) {
            if (Objects.isNull(discountStartDate) || Objects.isNull(discountEndDate)) {
                return DiscountResult.noDiscount(originalPrice);
            }
            LocalDate today = LocalDate.now();
            if (today.isBefore(discountStartDate) || today.isAfter(discountEndDate)) {
                return DiscountResult.noDiscount(originalPrice);
            }
        }

        int sellingPrice = (int) Math.floor(originalPrice * (100 - discountRate) / 100.0);
        return DiscountResult.discounted(sellingPrice, discountRate);
    }
}
