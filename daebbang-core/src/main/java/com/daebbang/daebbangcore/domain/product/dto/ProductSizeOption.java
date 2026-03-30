package com.daebbang.daebbangcore.domain.product.dto;

public record ProductSizeOption(
    String size,
    Integer stock,
    boolean soldOut
) {
}
