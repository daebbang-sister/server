package com.daebbang.daebbangcore.domain.cart.command;

public record CartSaveCommand(
    Long productDetailId,
    Integer quantity
) {

}
