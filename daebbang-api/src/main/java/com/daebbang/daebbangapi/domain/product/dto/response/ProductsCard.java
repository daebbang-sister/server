package com.daebbang.daebbangapi.domain.product.dto.response;

import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.util.DiscountCalculator;
import com.daebbang.daebbangcore.domain.product.util.DiscountResult;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductsCard(
    Long id,
    String categoryName,
    String productName,
    String mainImageUrl,
    String hoverImageUrl,
    Integer originalPrice,
    Integer sellingPrice,
    Integer discountRate,
    List<String> colorCodes
) {
    public static ProductsCard of(ProductCardQueryResult result) {
        DiscountResult discountResult = DiscountCalculator.calculate(
                                                                    result.originalPrice(),
                                                                    result.discountType(),
                                                                    result.discountRate(),
                                                                    result.discountStartDate(),
                                                                    result.discountEndDate()
                                                                    );

        return ProductsCard.builder()
            .id(result.id())
            .categoryName(result.categoryName())
            .productName(result.productName())
            .mainImageUrl(result.mainImageUrl())
            .hoverImageUrl(result.hoverImageUrl())
            .originalPrice(result.originalPrice())
            .sellingPrice(discountResult.sellingPrice())
            .discountRate(discountResult.discountRate())
            .colorCodes(result.colors())
            .build();
    }
}
