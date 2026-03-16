package com.daebbang.daebbangcore.domain.product.repository.dsl;

import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import java.time.LocalDate;
import java.util.List;

public interface ProductCustomRepository {

    List<ProductCardQueryResult> findNewProductsOnSale(LocalDate from, int limit);
    List<ProductCardQueryResult> findCategoryProductsOnSale(Long categoryId, int limit);
}
