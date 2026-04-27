package com.daebbang.daebbangapi.domain.wish.controller;

import com.daebbang.daebbangapi.domain.wish.dto.request.WishListSaveRequest;
import com.daebbang.daebbangapi.domain.wish.dto.response.WishCheckInfo;
import com.daebbang.daebbangapi.domain.wish.dto.response.WishIdInfo;
import com.daebbang.daebbangapi.domain.wish.dto.response.WishListInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.page.PageResponse;
import com.daebbang.daebbangcore.domain.wish.service.WishListService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/wish-lists")
public class WishListController {

    private final WishListService wishListService;

    @GetMapping
    public CommonResponse<PageResponse<WishListInfo>> getWishList(
        @AuthenticationPrincipal Long userId,
        Pageable pageable
    ) {
        PageResponse<WishListInfo> response = PageResponse.from(
            wishListService.getWishList(userId, pageable).map(WishListInfo::from)
        );
        return CommonResponse.success(CommonSuccessCode.SELECT_SUCCESS, response);
    }

    @GetMapping("/check")
    public CommonResponse<WishCheckInfo> checkWishList(
        @AuthenticationPrincipal Long userId,
        @RequestParam @Positive(message = "상품 ID는 1 이상이어야 합니다.") Long productId
    ) {
        var result = wishListService.isWished(userId, productId);
        return CommonResponse.success(UserSuccessCode.CHECK_WISH_LIST, new WishCheckInfo(result.isPresent(), result.orElse(null)));
    }

    @PostMapping
    public ResponseEntity<@NonNull CommonResponse<WishIdInfo>> addWishList(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid WishListSaveRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.success(
                UserSuccessCode.ADD_WISH_LIST,
                WishIdInfo.toDto(wishListService.addWishList(userId, request.productId())
            )));
    }

    @DeleteMapping
    public CommonResponse<Void> deleteWishLists(
        @AuthenticationPrincipal Long userId,
        @RequestParam(name = "ids")
        @NotEmpty(message = "삭제할 항목 ID를 하나 이상 입력해주세요.")
        List<@Positive(message = "삭제할 항목 ID는 1 이상이어야 합니다.") Long> wishListIds
    ) {
        wishListService.deleteWishLists(userId, wishListIds);
        return CommonResponse.success(UserSuccessCode.DELETE_WISH_LIST);
    }

    @DeleteMapping("/all")
    public CommonResponse<Void> deleteAllWishLists(
        @AuthenticationPrincipal Long userId
    ) {
        wishListService.deleteAllWishLists(userId);
        return CommonResponse.success(UserSuccessCode.DELETE_WISH_LIST);
    }
}
