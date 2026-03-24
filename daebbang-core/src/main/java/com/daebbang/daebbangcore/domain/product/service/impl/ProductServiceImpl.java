package com.daebbang.daebbangcore.domain.product.service.impl;

import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.category.service.CategoryService;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import com.daebbang.daebbangcore.domain.product.repository.ProductRepository;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final int NEW_PRODUCT_DAYS = 30;

    private final ProductRepository productRepository;

    private final CategoryService categoryService;

    @Override
    public List<ProductCardQueryResult> getOnSaleNewProducts(int limit) {

        LocalDate from = LocalDate.now().minusDays(NEW_PRODUCT_DAYS);

        return productRepository.findOnSaleNewProducts(from, limit);
    }

    @Override
    public List<ProductCardQueryResult> getOnSaleCategoryProducts(Long categoryId, int limit) {
        return productRepository.findOnSaleCategoryProducts(categoryId, limit);
    }

    @Override
    public Page<@NonNull ProductCardQueryResult> getOnSaleProductsByCategory(Long categoryId,
        ProductSortType sort, SortDirection direction, Pageable pageable) {

        Category selectCategory = null;
        if (Objects.nonNull(categoryId)) {
            selectCategory = categoryService.getActiveCategoryById(categoryId);
        }

        return productRepository.findOnSaleProductsByCategory(selectCategory, sort, direction, pageable);
    }
}
