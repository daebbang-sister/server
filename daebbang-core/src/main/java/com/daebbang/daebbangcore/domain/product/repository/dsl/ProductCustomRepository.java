package com.daebbang.daebbangcore.domain.product.repository.dsl;

import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductDetailQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductGalleryImageResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductOptionRaw;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface ProductCustomRepository {

    List<ProductCardQueryResult> findOnSaleNewProducts(LocalDate from, int limit);
    List<ProductCardQueryResult> findOnSaleCategoryProducts(Long categoryId, int limit);
    Page<@NonNull ProductCardQueryResult> findOnSaleNewProductsPage(LocalDate from, ProductSortType sort, SortDirection direction, Pageable pageable);
    Page<@NonNull ProductCardQueryResult> findOnSaleProductsByCategory(Category category, ProductSortType sort, SortDirection direction, Pageable pageable);

    Page<@NonNull ProductCardQueryResult> searchProducts(String keyword, ProductSortType sort, SortDirection direction, Pageable pageable);

    Optional<ProductDetailQueryResult> findProductDetailById(Long productId);
    List<ProductGalleryImageResult> findGalleryImages(Long productId);
    List<ProductOptionRaw> findProductOptions(Long productId);

    List<ProductCardQueryResult> findBestProducts(@Nullable Long categoryId, int limit, int periodDays);
}
