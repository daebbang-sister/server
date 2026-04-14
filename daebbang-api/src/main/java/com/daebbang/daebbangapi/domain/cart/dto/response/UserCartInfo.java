package com.daebbang.daebbangapi.domain.cart.dto.response;

import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.util.DiscountCalculator;
import com.daebbang.daebbangcore.domain.product.util.DiscountResult;

public record UserCartInfo(
    Long cartId,
    Long productId,
    Long productDetailId,
    Integer quantity,
    String productName,
    Integer originalPrice,
    Integer discountPrice,
    Integer discountRate,
    String color,
    String size,
    String mainImageUrl
) {

    public static UserCartInfo from(Carts cart) {
        Products product = cart.getProductDetail().getProduct();
        DiscountResult discount = DiscountCalculator.calculate(
            product.getOriginalPrice(),
            product.getDiscountType(),
            product.getDiscountRate(),
            product.getDiscountStartDate(),
            product.getDiscountEndDate()
        );

        return new UserCartInfo(
            cart.getId(),
            product.getId(),
            cart.getProductDetail().getId(),
            cart.getQuantity(),
            product.getProductName(),
            product.getOriginalPrice(),
            discount.sellingPrice(),
            discount.discountRate(),
            cart.getProductDetail().getColor(),
            cart.getProductDetail().getSize(),
            product.getMainImageUrl()
        );
    }
}
