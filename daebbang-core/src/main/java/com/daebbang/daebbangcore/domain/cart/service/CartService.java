package com.daebbang.daebbangcore.domain.cart.service;

import com.daebbang.daebbangcore.domain.cart.command.CartSaveCommand;
import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import java.util.List;

public interface CartService {
    List<Carts> getCarts(Long userId, Long cursor, int size);
    void saveCarts(Long userId, List<CartSaveCommand> commands);
    void updateCarts(Long userId, Long cartId, Long productDetailsId, Integer quantity);
    void deleteCartsByCartsId(List<Long> cartsId, Long userId);
    void deleteAllCartsByUser(Long userId);
}
