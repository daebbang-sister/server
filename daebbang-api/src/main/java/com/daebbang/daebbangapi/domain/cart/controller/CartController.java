package com.daebbang.daebbangapi.domain.cart.controller;

import com.daebbang.daebbangapi.domain.cart.dto.request.CartSaveRequest;
import com.daebbang.daebbangapi.domain.cart.dto.request.CartUpdate;
import com.daebbang.daebbangapi.domain.cart.dto.response.CartPageResponse;
import com.daebbang.daebbangapi.domain.cart.dto.response.UserCartInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import com.daebbang.daebbangcore.domain.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping
    public CommonResponse<CartPageResponse> getCarts(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "8") int size) {

        List<Carts> result = cartService.getCarts(userId, cursor, size + 1);

        boolean hasNext = result.size() > size;
        List<Carts> content = hasNext ? result.subList(0, size) : result;
        Long nextCursor = hasNext ? content.get(content.size() - 1).getId() : null;

        List<UserCartInfo> carts = content.stream()
            .map(UserCartInfo::from)
            .toList();

        return CommonResponse.success(CommonSuccessCode.SELECT_SUCCESS,
            new CartPageResponse(carts, nextCursor, hasNext));
    }

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
    public CommonResponse<Void> deleteCarts(@AuthenticationPrincipal Long userId,
        @RequestParam(name = "ids") @NotEmpty(message = "삭제할 항목 ID를 하나 이상 입력해주세요.") List<Long> cartIds) {
        cartService.deleteCartsByCartsId(cartIds, userId);
        return CommonResponse.success(UserSuccessCode.DELETE_CART);
    }

    @DeleteMapping("/all")
    public CommonResponse<Void> deleteAllCarts(@AuthenticationPrincipal Long userId) {
        cartService.deleteAllCartsByUser(userId);
        return CommonResponse.success(UserSuccessCode.DELETE_CART);
    }
}
