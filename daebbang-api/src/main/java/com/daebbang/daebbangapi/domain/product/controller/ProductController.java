package com.daebbang.daebbangapi.domain.product.controller;

import com.daebbang.daebbangapi.domain.product.dto.response.ProductMainCard;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    public CommonResponse<List<ProductMainCard>> getMainNewProductsOnSale(@RequestParam("limit") int limit) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            productService.getNewProductsOnSale(limit)
                .stream()
                .map(ProductMainCard::of)
                .toList());
    }

    @GetMapping("/main/category/{categoryId}")
    public CommonResponse<List<ProductMainCard>> getMainCategoryProductsOnSale(
        @PathVariable Long categoryId, @RequestParam("limit") int limit) {
        return CommonResponse.success(
            CommonSuccessCode.SELECT_SUCCESS,
            productService.getCategoryProductsOnSale(categoryId, limit)
                .stream()
                .map(ProductMainCard::of)
                .toList()
        );
    }
}
