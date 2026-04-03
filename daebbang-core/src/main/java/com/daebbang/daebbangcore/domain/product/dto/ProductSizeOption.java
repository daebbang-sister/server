package com.daebbang.daebbangcore.domain.product.dto;

public record ProductSizeOption(
    Long productDetailId,
    String size,
    Integer stock,
    boolean soldOut
) {
}
