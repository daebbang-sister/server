package com.daebbang.daebbangcore.domain.product.repository.dsl.impl;

import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import com.daebbang.daebbangcore.domain.product.repository.dsl.ProductCustomRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            .on(category.eq(productCategory.category));
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
                    products.productStatus.eq(ProductStatus.SALE)
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
            case POPULAR -> products.createdAt.desc(); // TODO: 인기순 구현 전 임시 fallback
        };
    }
}
