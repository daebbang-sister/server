package com.daebbang.daebbangcore.domain.product.service;

import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import java.util.List;

public interface ProductService {

    List<ProductCardQueryResult> getNewProductsOnSale(int limit);
    List<ProductCardQueryResult> getCategoryProductsOnSale(Long categoryId, int limit);
}
