package com.daebbang.daebbangcore.domain.product.repository.dsl;

import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ProductCustomRepository {

    List<ProductCardQueryResult> findOnSaleNewProducts(LocalDate from, int limit);
    List<ProductCardQueryResult> findOnSaleCategoryProducts(Long categoryId, int limit);
    Page<@NonNull ProductCardQueryResult> findOnSaleProductsByCategory(Category category, ProductSortType sort, SortDirection direction, Pageable pageable);
}
