package com.daebbang.daebbangapi.domain.cart.controller;

import com.daebbang.daebbangapi.domain.cart.dto.request.CartSaveRequest;
import com.daebbang.daebbangapi.domain.cart.dto.request.CartUpdate;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.cart.service.CartService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<@NonNull CommonResponse<Void>> saveCarts(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid List<CartSaveRequest> requests) {
        cartService.saveCarts(userId, requests.stream().map(CartSaveRequest::toCommand).toList());
        return ResponseEntity.status(HttpStatus.CREATED)
                            .body(CommonResponse.success(UserSuccessCode.ADD_CART));
    }

    @PatchMapping("/{cartId}")
    public CommonResponse<Void> updateCarts(@AuthenticationPrincipal Long userId,
        @PathVariable Long cartId, @RequestBody CartUpdate update) {
        cartService.updateCarts(userId, cartId, update.productDetailsId(), update.quantity());
        return CommonResponse.success(UserSuccessCode.UPDATE_CART);
    }

    @DeleteMapping
    public CommonResponse<Void> deleteCarts(@AuthenticationPrincipal Long userId, @RequestParam(name = "ids")List<Long> cartIds) {
        cartService.deleteCartsByCartsId(cartIds, userId);
        return CommonResponse.success(UserSuccessCode.DELETE_CART);
    }

    @DeleteMapping("/all")
    public CommonResponse<Void> deleteAllCarts(@AuthenticationPrincipal Long userId) {
        cartService.deleteAllCartsByUser(userId);
        return CommonResponse.success(UserSuccessCode.DELETE_CART);
    }
}
