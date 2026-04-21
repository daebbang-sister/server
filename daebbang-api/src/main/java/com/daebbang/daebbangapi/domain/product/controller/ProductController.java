package com.daebbang.daebbangapi.domain.product.controller;

import com.daebbang.daebbangapi.domain.product.dto.response.ProductDetailResponse;
import com.daebbang.daebbangapi.domain.product.dto.response.ProductOptionsResponse;
import com.daebbang.daebbangapi.domain.product.dto.response.ProductsCard;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcore.domain.page.PageResponse;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import com.daebbang.daebbangcore.domain.wish.service.WishListService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/products")
public class ProductController {

    private final ProductService productService;
    private final WishListService wishListService;

    @GetMapping("/main/new")
    public CommonResponse<List<ProductsCard>> getMainNewProductsOnSale(@RequestParam("limit") int limit) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            productService.getOnSaleNewProducts(limit)
                .stream()
                .map(ProductsCard::of)
                .toList());
    }

    @GetMapping("/main/category/{categoryId}")
    public CommonResponse<List<ProductsCard>> getMainCategoryProductsOnSale(
        @PathVariable Long categoryId, @RequestParam("limit") int limit) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            productService.getOnSaleCategoryProducts(categoryId, limit)
                .stream()
                .map(ProductsCard::of)
                .toList()
        );
    }

    @GetMapping("/new")
    public CommonResponse<PageResponse<ProductsCard>> getNewProductsOnSale(
        @RequestParam(defaultValue = "DESC") SortDirection direction,
        Pageable pageable) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            PageResponse.from(
                productService.getOnSaleProductsByCategory(null, ProductSortType.NEW, direction, pageable)
                                .map(ProductsCard::of)
            )
        );
    }

    @GetMapping("/{productId}")
    public CommonResponse<ProductDetailResponse> getProductDetail(
        @PathVariable Long productId,
        @AuthenticationPrincipal Long userId
    ) {
        Boolean isWished = userId != null && wishListService.isWished(userId, productId);

        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            ProductDetailResponse.of(productService.getProductDetail(productId), isWished)
        );
    }

    @GetMapping("/{productId}/options")
    public CommonResponse<List<ProductOptionsResponse>> getProductOptions(@PathVariable Long productId) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            productService.getProductOptions(productId).stream()
                .map(ProductOptionsResponse::from)
                .toList()
        );
    }

    @GetMapping("/search")
    public CommonResponse<PageResponse<ProductsCard>> searchProducts(
        @RequestParam @NotBlank(message = "검색어를 입력해주세요.") @Size(max = 50, message = "검색어는 50자 이하로 입력해주세요.") String keyword,
        @RequestParam(defaultValue = "NEW") ProductSortType sortType,
        @RequestParam(defaultValue = "DESC") SortDirection direction,
        Pageable pageable
    ) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            PageResponse.from(
                productService.searchProducts(keyword, sortType, direction, pageable)
                              .map(ProductsCard::of)
            )
        );
    }

    @GetMapping("/category/{categoryId}")
    public CommonResponse<PageResponse<ProductsCard>> getCategoryProductsOnSale(
        @PathVariable Long categoryId,
        @RequestParam(defaultValue = "NEW") ProductSortType sortType,
        @RequestParam(defaultValue = "DESC") SortDirection direction,
        Pageable pageable
    ) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            PageResponse.from(
                productService.getOnSaleProductsByCategory(categoryId, sortType, direction, pageable)
                                .map(ProductsCard::of)
            )
        );
    }
}
