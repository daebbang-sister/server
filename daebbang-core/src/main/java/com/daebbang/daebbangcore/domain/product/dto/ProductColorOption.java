package com.daebbang.daebbangcore.domain.product.dto;

import java.util.List;

public record ProductColorOption(
    String color,
    String colorCode,
    List<ProductSizeOption> sizes
) {
}
