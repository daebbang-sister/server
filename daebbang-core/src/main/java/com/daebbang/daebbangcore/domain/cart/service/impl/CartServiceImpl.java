package com.daebbang.daebbangcore.domain.cart.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CartErrorCode;
import com.daebbang.daebbangcore.domain.cart.command.CartSaveCommand;
import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import com.daebbang.daebbangcore.domain.cart.repository.CartRepository;
import com.daebbang.daebbangcore.domain.cart.service.CartService;
import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
import com.daebbang.daebbangcore.domain.product.service.ProductDetailsService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserService userService;
    private final ProductDetailsService productDetailsService;

    @Override
    public List<Carts> getCarts(Long userId, Long cursor, int size) {
        return cartRepository.findCartsByUserIdWithCursor(userId, cursor, PageRequest.of(0, size));
    }

    @Override
    @Transactional
    public void saveCarts(Long userId, List<CartSaveCommand> commands) {
        List<Long> productDetailIds = commands.stream()
            .map(CartSaveCommand::productDetailId)
            .toList();

        Map<Long, Carts> existingCartMap = cartRepository
            .findByUserIdAndProductDetailIdIn(userId, productDetailIds)
            .stream()
            .collect(Collectors.toMap(cart -> cart.getProductDetail().getId(), cart -> cart));

        Users user = userService.getUserById(userId);
        List<Carts> newCarts = new ArrayList<>();

        for (CartSaveCommand command : commands) {
            if (existingCartMap.containsKey(command.productDetailId())) {
                existingCartMap.get(command.productDetailId()).addQuantity(command.quantity());
            } else {
                ProductDetails productDetail = productDetailsService.getProductDetailsById(command.productDetailId());
                newCarts.add(Carts.create(user, productDetail, command.quantity()));
            }
        }

        if (!newCarts.isEmpty()) {
            cartRepository.saveAll(newCarts);
        }
    }

    @Override
    @Transactional
    public void updateCarts(Long userId, Long cartId, Long productDetailsId, Integer quantity) {
        Carts carts = getCarts(cartId, userId);
        ProductDetails productDetails = productDetailsService.getProductDetailsById(productDetailsId);
        carts.updateQuantity(quantity);
        carts.updateProductDetail(productDetails);
    }

    @Override
    @Transactional
    public void deleteCartsByCartsId(List<Long> cartsId, Long userId) {
        cartRepository.deleteCartsByCartIds(cartsId, userId);
    }

    @Override
    @Transactional
    public void deleteAllCartsByUser(Long userId) {
        cartRepository.deleteAllCartsByUserId(userId);
    }

    private Carts getCarts(Long cartId, Long userId) {
        return cartRepository.findByIdAndUserId(cartId, userId)
            .orElseThrow(() -> new BusinessException(CartErrorCode.CART_NOT_FOUND));
    }
}
