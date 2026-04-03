package com.daebbang.daebbangapi.domain.product.dto.response;

import com.daebbang.daebbangcore.domain.product.dto.ProductDetailResult;
import com.daebbang.daebbangcore.domain.product.util.DiscountCalculator;
import com.daebbang.daebbangcore.domain.product.util.DiscountResult;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductDetailResponse(
    Long id,
    String categoryName,
    String productName,
    String simpleDescription,
    String mainImageUrl,
    Integer originalPrice,
    Integer sellingPrice,
    Integer discountRate,
    List<GalleryImage> gallery,
    String descriptionHtml,
    List<ColorOption> options
) {
    public static ProductDetailResponse of(ProductDetailResult result) {
        DiscountResult discount = DiscountCalculator.calculate(
            result.originalPrice(),
            result.discountType(),
            result.discountRate(),
            result.discountStartDate(),
            result.discountEndDate()
        );

        return ProductDetailResponse.builder()
            .id(result.id())
            .categoryName(result.categoryName())
            .productName(result.productName())
            .simpleDescription(result.simpleDescription())
            .mainImageUrl(result.mainImageUrl())
            .originalPrice(result.originalPrice())
            .sellingPrice(discount.sellingPrice())
            .discountRate(discount.discountRate())
            .gallery(result.gallery().stream()
                .map(g -> new GalleryImage(g.imageUrl(), g.imageOrder()))
                .toList())
            .descriptionHtml(result.descriptionHtml())
            .options(result.options().stream()
                .map(o -> new ColorOption(
                    o.color(),
                    o.colorCode(),
                    o.sizes().stream()
                        .map(s -> new SizeOption(s.productDetailId(), s.size(), s.stock(), s.soldOut()))
                        .toList()
                ))
                .toList())
            .build();
    }

    public record GalleryImage(String imageUrl, Short imageOrder) {}

    public record ColorOption(String color, String colorCode, List<SizeOption> sizes) {}

    public record SizeOption(Long productDetailId, String size, Integer stock, boolean soldOut) {}
}
