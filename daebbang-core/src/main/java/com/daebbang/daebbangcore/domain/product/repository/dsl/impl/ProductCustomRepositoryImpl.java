package com.daebbang.daebbangcore.domain.product.repository.dsl.impl;

import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductDetailQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductGalleryImageResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductOptionRaw;
import com.daebbang.daebbangcore.domain.product.entity.ImageType;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import com.daebbang.daebbangcore.domain.product.repository.dsl.ProductCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

// static
import static com.daebbang.daebbangcore.domain.product.entity.QProducts.products;
import static com.daebbang.daebbangcore.domain.category.entity.QCategory.category;
import static com.daebbang.daebbangcore.domain.product.entity.QProductCategory.productCategory;
import static com.daebbang.daebbangcore.domain.product.entity.QProductDetails.productDetails;
import static com.daebbang.daebbangcore.domain.product.entity.QProductImage.productImage;
import static com.daebbang.daebbangcore.domain.product.entity.QProductSalesLog.productSalesLog;
import static com.daebbang.daebbangcore.domain.product.entity.QProductStats.productStats;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    private JPAQuery<ProductCardQueryResult> baseQuery() {
        return queryFactory
            .select(
                Projections.constructor(
                    ProductCardQueryResult.class,
                    products.id,
                    category.categoryName,
                    products.productName,
                    products.mainImageUrl,
                    products.hoverImageUrl,
                    products.originalPrice,
                    products.discountType,
                    products.discountRate,
                    products.discountStartDate,
                    products.discountEndDate,
                    products.productStatus,
                    Expressions.constant(List.of())))
            .from(products)
            .innerJoin(productCategory)
            .on(productCategory.product.eq(products))
            .innerJoin(category)
            .on(category.eq(productCategory.category))
            .leftJoin(productStats)
            .on(productStats.product.eq(products));
    }

    private Map<Long, List<String>> fetchColors(List<Long> productIds) {
        return queryFactory
                        .select(productDetails.product.id, productDetails.colorCode)
                        .from(productDetails)
                        .where(
                            productDetails.product.id.in(productIds),
                            productDetails.deletedAt.isNull()
                        )
                        .distinct()
                        .fetch()
                        .stream()
                        .collect(Collectors.groupingBy(
                            target -> Objects.requireNonNull(target.get(productDetails.product.id)),
                            Collectors.mapping(
                                target -> Objects.requireNonNull(target.get(productDetails.colorCode)),
                                Collectors.toList()
                            )
                        ));
    }

    private List<ProductCardQueryResult> attachColors(List<ProductCardQueryResult> results) {
        if (results.isEmpty()) {
            return results;
        }

        List<Long> ids = results.stream().map(ProductCardQueryResult::id).toList();
        Map<Long, List<String>> colorMap = fetchColors(ids);

        return results.stream()
            .map(r -> new ProductCardQueryResult(
                r.id(), r.categoryName(), r.productName(),
                r.mainImageUrl(), r.hoverImageUrl(), r.originalPrice(),
                r.discountType(), r.discountRate(),
                r.discountStartDate(), r.discountEndDate(),
                r.productStatus(),
                colorMap.getOrDefault(r.id(), List.of())
            ))
            .toList();
    }

    @Override
    public List<ProductCardQueryResult> findOnSaleNewProducts(LocalDate from, int limit) {
        return attachColors(
            baseQuery()
                .where(
                    products.createdAt.goe(from.atStartOfDay()),
                    products.productStatus.eq(ProductStatus.SALE),
                    products.discountStartDate.isNotNull()
                        .and(products.discountStartDate.eq(
                            Expressions.dateTemplate(LocalDate.class, "DATE({0})", products.createdAt)))
                )
                .orderBy(products.createdAt.desc())
                .limit(limit)
                .fetch()
        );
    }

    @Override
    public List<ProductCardQueryResult> findOnSaleCategoryProducts(Long categoryId, int limit) {
        return attachColors(
            baseQuery()
                .where(
                    category.id.eq(categoryId),
                    products.productStatus.eq(ProductStatus.SALE)
                )
                .orderBy(products.createdAt.desc())
                .limit(limit)
                .fetch()
        );
    }

    @Override
    public Page<@NonNull ProductCardQueryResult> findOnSaleNewProductsPage(LocalDate from, ProductSortType sort,
        SortDirection direction, Pageable pageable) {

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(products.productStatus.eq(ProductStatus.SALE));
        condition.and(products.createdAt.goe(from.atStartOfDay()));
        // 등록일부터 할인이 시작되어야 신상품으로 인정
        condition.and(
            products.discountStartDate.isNotNull()
                .and(products.discountStartDate.eq(
                    Expressions.dateTemplate(LocalDate.class, "DATE({0})", products.createdAt)))
        );

        // 다중 카테고리 중복 방지를 위한 2-step 조회 (distinct id → 카드 fetch)
        List<Long> productIds = queryFactory
            .select(products.id)
            .distinct()
            .from(products)
            .innerJoin(productCategory).on(productCategory.product.eq(products))
            .innerJoin(category).on(category.eq(productCategory.category))
            .leftJoin(productStats).on(productStats.product.eq(products))
            .where(condition)
            .orderBy(resolveSort(sort, direction))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<ProductCardQueryResult> content = productIds.isEmpty()
            ? List.of()
            : attachColors(
                baseQuery()
                    .where(products.id.in(productIds))
                    .orderBy(resolveSort(sort, direction), category.id.asc())
                    .fetch()
                    .stream()
                    .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                            ProductCardQueryResult::id,
                            r -> r,
                            (a, b) -> a,
                            LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                    ))
            );

        Long total = queryFactory
            .select(products.countDistinct())
            .from(products)
            .innerJoin(productCategory).on(productCategory.product.eq(products))
            .innerJoin(category).on(category.eq(productCategory.category))
            .where(condition)
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    @Override
    public Page<@NonNull ProductCardQueryResult> findOnSaleProductsByCategory(Category selectCategory,
        ProductSortType sort, SortDirection direction, Pageable pageable) {

        BooleanBuilder condition = new BooleanBuilder();
        if (Objects.nonNull(selectCategory)) {
            condition.and(productCategory.category.eq(selectCategory));
        }
        condition.and(products.productStatus.eq(ProductStatus.SALE));

        List<ProductCardQueryResult> content = attachColors(
            baseQuery()
                .where(condition)
                .orderBy(resolveSort(sort, direction))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
        );

        Long total = queryFactory
            .select(products.count())
            .from(products)
            .innerJoin(productCategory)
                .on(productCategory.product.eq(products))
            .innerJoin(category)
                .on(category.eq(productCategory.category))
            .where(condition)
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    @Override
    public Page<@NonNull ProductCardQueryResult> searchProducts(String keyword, ProductSortType sort, SortDirection direction, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(products.productName.containsIgnoreCase(keyword));
        condition.and(products.productStatus.eq(ProductStatus.SALE));

        List<Long> productIds = queryFactory
            .select(products.id, products.createdAt, products.productName, products.originalPrice)
            .distinct()
            .from(products)
            .innerJoin(productCategory).on(productCategory.product.eq(products))
            .innerJoin(category).on(category.eq(productCategory.category))
            .leftJoin(productStats).on(productStats.product.eq(products))
            .where(condition)
            .orderBy(resolveSort(sort, direction))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch()
            .stream()
            .map(tuple -> tuple.get(products.id))
            .toList();

        List<ProductCardQueryResult> content = productIds.isEmpty()
            ? List.of()
            : attachColors(
                baseQuery()
                    .where(products.id.in(productIds))
                    .orderBy(resolveSort(sort, direction), category.id.asc())
                    .fetch()
                    .stream()
                    .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                            ProductCardQueryResult::id,
                            r -> r,
                            (a, b) -> a,
                            LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                    ))
            );

        Long total = queryFactory
            .select(products.id.countDistinct())
            .from(products)
            .innerJoin(productCategory).on(productCategory.product.eq(products))
            .innerJoin(category).on(category.eq(productCategory.category))
            .where(condition)
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    @Override
    public Optional<ProductDetailQueryResult> findProductDetailById(Long productId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ProductDetailQueryResult.class,
                    products.id,
                    category.categoryName,
                    products.productName,
                    products.simpleDescription,
                    products.mainImageUrl,
                    products.originalPrice,
                    products.discountType,
                    products.discountRate,
                    products.discountStartDate,
                    products.discountEndDate,
                    products.descriptionHtml
                ))
                .from(products)
                .innerJoin(productCategory).on(productCategory.product.eq(products))
                .innerJoin(category).on(category.eq(productCategory.category))
                .where(
                    products.id.eq(productId),
                    products.deletedAt.isNull()
                )
                .fetchOne()
        );
    }

    @Override
    public List<ProductGalleryImageResult> findGalleryImages(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductGalleryImageResult.class,
                productImage.imageUrl,
                productImage.imageOrder
            ))
            .from(productImage)
            .where(
                productImage.product.id.eq(productId),
                productImage.imageType.eq(ImageType.DETAILS),
                productImage.deletedAt.isNull()
            )
            .orderBy(productImage.imageOrder.asc())
            .fetch();
    }

    @Override
    public List<ProductOptionRaw> findProductOptions(Long productId) {
        return queryFactory
            .select(Projections.constructor(ProductOptionRaw.class,
                productDetails.id,
                productDetails.color,
                productDetails.colorCode,
                productDetails.size,
                productDetails.stock
            ))
            .from(productDetails)
            .where(
                productDetails.product.id.eq(productId),
                productDetails.deletedAt.isNull()
            )
            .orderBy(productDetails.color.asc(), productDetails.size.asc())
            .fetch();
    }

    @Override
    public List<ProductCardQueryResult> findBestProducts(@Nullable Long categoryId, int limit, int periodDays) {
        LocalDate from = LocalDate.now().minusDays(periodDays);

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(products.productStatus.eq(ProductStatus.SALE));
        condition.and(products.deletedAt.isNull());
        if (Objects.nonNull(categoryId)) {
            condition.and(category.id.eq(categoryId));
        }

        // Step 1: 기간 내 판매량 합계 기준으로 상품 ID 순위 결정
        var periodSalesSum = Expressions.numberTemplate(Long.class, "SUM({0})", productSalesLog.salesCount);
        List<Long> orderedIds = queryFactory
            .select(productSalesLog.product.id)
            .from(productSalesLog)
            .innerJoin(products).on(products.id.eq(productSalesLog.product.id))
            .innerJoin(productCategory).on(productCategory.product.eq(products))
            .innerJoin(category).on(category.eq(productCategory.category))
            .where(
                productSalesLog.saleDate.goe(from),
                condition
            )
            .groupBy(productSalesLog.product.id)
            .orderBy(periodSalesSum.desc())
            .limit(limit)
            .fetch();

        if (orderedIds.isEmpty()) {
            return List.of();
        }

        // Step 2: 카드 데이터 조회 후 Step 1 순서 복원
        Map<Long, ProductCardQueryResult> cardMap = attachColors(
            baseQuery()
                .where(products.id.in(orderedIds))
                .fetch()
        ).stream().collect(Collectors.toMap(ProductCardQueryResult::id, r -> r, (a, b) -> a));

        return orderedIds.stream()
            .map(cardMap::get)
            .filter(Objects::nonNull)
            .toList();
    }

    private OrderSpecifier<?> resolveSort(ProductSortType sort, SortDirection direction) {
        return switch (sort) {
            case NEW -> products.createdAt.desc();
            case LATEST -> direction == SortDirection.ASC
                ? products.createdAt.asc()
                : products.createdAt.desc();
            case NAME -> direction == SortDirection.ASC
                ? products.productName.asc()
                : products.productName.desc();
            case PRICE -> direction == SortDirection.ASC
                ? products.originalPrice.asc()
                : products.originalPrice.desc();
            case POPULAR -> productStats.reviewCount.desc().nullsLast();
        };
    }
}
