package com.daebbang.daebbangapi.domain.product.dto.response;

import com.daebbang.daebbangcore.domain.product.dto.ProductColorOption;
import java.util.List;

public record ProductOptionsResponse(
    String color,
    String colorCode,
    List<SizeOption> sizes
) {

    public static ProductOptionsResponse from(ProductColorOption option) {
        return new ProductOptionsResponse(
            option.color(),
            option.colorCode(),
            option.sizes().stream()
                .map(s -> new SizeOption(s.productDetailId(), s.size(), s.stock(), s.soldOut()))
                .toList()
        );
    }

    public record SizeOption(Long productDetailId, String size, Integer stock, boolean soldOut) {}
}
