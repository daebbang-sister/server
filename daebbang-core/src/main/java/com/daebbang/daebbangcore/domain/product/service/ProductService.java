package com.daebbang.daebbangcore.domain.product.service;

import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    List<ProductCardQueryResult> getOnSaleNewProducts(int limit);
    List<ProductCardQueryResult> getOnSaleCategoryProducts(Long categoryId, int limit);
    Page<@NonNull ProductCardQueryResult> getOnSaleProductsByCategory(Long categoryId, ProductSortType sort, SortDirection direction, Pageable pageable);
}
