package com.daebbang.daebbangcore.domain.product.dto;

public record ProductOptionRaw(
    Long productDetailId,
    String color,
    String colorCode,
    String size,
    Integer stock
) {
}
