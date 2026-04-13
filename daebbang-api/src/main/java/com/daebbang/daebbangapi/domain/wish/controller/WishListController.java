package com.daebbang.daebbangapi.domain.wish.controller;

import com.daebbang.daebbangapi.domain.wish.dto.request.WishListSaveRequest;
import com.daebbang.daebbangapi.domain.wish.dto.response.WishListInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.page.PageResponse;
import com.daebbang.daebbangcore.domain.wish.service.WishListService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @PostMapping
    public ResponseEntity<@NonNull CommonResponse<Void>> addWishList(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid WishListSaveRequest request
    ) {
        wishListService.addWishList(userId, request.productId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.success(UserSuccessCode.ADD_WISH_LIST));
    }

    @DeleteMapping
    public CommonResponse<Void> deleteWishLists(
        @AuthenticationPrincipal Long userId,
        @RequestParam(name = "ids") @NotEmpty(message = "삭제할 항목 ID를 하나 이상 입력해주세요.") List<Long> wishListIds
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
