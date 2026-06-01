package com.daebbang.daebbangcore.domain.product.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ProductErrorCode;
import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.category.service.CategoryService;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductColorOption;
import com.daebbang.daebbangcore.domain.product.dto.ProductDetailQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductDetailResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductOptionRaw;
import com.daebbang.daebbangcore.domain.product.dto.ProductSizeOption;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import com.daebbang.daebbangcore.domain.product.event.ProductViewedEvent;
import com.daebbang.daebbangcore.domain.product.repository.ProductRepository;
import com.daebbang.daebbangcore.domain.product.service.ProductBestSettingsService;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final int NEW_PRODUCT_DAYS = 30;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProductBestSettingsService productBestSettingsService;

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
    public Page<@NonNull ProductCardQueryResult> getOnSaleNewProductsPage(ProductSortType sort, SortDirection direction, Pageable pageable) {
        LocalDate from = LocalDate.now().minusDays(NEW_PRODUCT_DAYS);
        return productRepository.findOnSaleNewProductsPage(from, sort, direction, pageable);
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

    @Override
    public Page<@NonNull ProductCardQueryResult> searchProducts(String keyword, ProductSortType sort, SortDirection direction, Pageable pageable) {
        return productRepository.searchProducts(keyword, sort, direction, pageable);
    }

    @Override
    public Products getOnSaleProductById(Long productId) {
        return productRepository.findByIdAndStatusAndNotDeleted(productId, ProductStatus.SALE)
            .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductDetailResult getProductDetail(Long productId) {
        ProductDetailQueryResult detail = productRepository.findProductDetailById(productId)
            .orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));

        eventPublisher.publishEvent(new ProductViewedEvent(productId));

        return new ProductDetailResult(
            detail.id(),
            detail.categoryName(),
            detail.productName(),
            detail.simpleDescription(),
            detail.mainImageUrl(),
            detail.originalPrice(),
            detail.discountType(),
            detail.discountRate(),
            detail.discountStartDate(),
            detail.discountEndDate(),
            detail.descriptionHtml(),
            productRepository.findGalleryImages(productId),
            buildColorOptions(productRepository.findProductOptions(productId))
        );
    }

    @Override
    public List<ProductColorOption> getProductOptions(Long productId) {
        return buildColorOptions(productRepository.findProductOptions(productId));
    }

    @Override
    public List<ProductCardQueryResult> getBestProducts(@Nullable Long categoryId, int limit, @Nullable Integer periodDays) {
        int resolvedPeriodDays = productBestSettingsService.resolvePeriodDays(periodDays);
        return productRepository.findBestProducts(categoryId, limit, resolvedPeriodDays);
    }

    private List<ProductColorOption> buildColorOptions(List<ProductOptionRaw> raws) {
        Map<String, List<ProductOptionRaw>> byColor = new LinkedHashMap<>();
        for (ProductOptionRaw raw : raws) {
            byColor.computeIfAbsent(raw.colorCode(), k -> new ArrayList<>()).add(raw);
        }

        return byColor.values().stream()
            .map(group -> new ProductColorOption(
                group.get(0).color(),
                group.get(0).colorCode(),
                group.stream()
                    .map(r -> new ProductSizeOption(r.productDetailId(), r.size(), r.stock(), r.stock() == 0))
                    .toList()
            ))
            .toList();
    }
}
