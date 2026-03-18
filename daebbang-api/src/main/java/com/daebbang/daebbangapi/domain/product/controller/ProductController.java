package com.daebbang.daebbangapi.domain.product.controller;

import com.daebbang.daebbangapi.domain.product.dto.response.ProductsCard;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcore.domain.page.PageResponse;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/products")
public class ProductController {

    private final ProductService productService;

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
        @RequestParam(defaultValue = "ASC") SortDirection direction,
        Pageable pageable) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            PageResponse.from(
                productService.getOnSaleProductsByCategory(null, ProductSortType.NEW, direction, pageable)
                    .map(ProductsCard::of)
            )
        );
    }
}
