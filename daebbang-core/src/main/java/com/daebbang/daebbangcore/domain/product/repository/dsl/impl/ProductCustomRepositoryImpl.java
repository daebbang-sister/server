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
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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
                    products.productStatus))
            .from(products)
            .innerJoin(productCategory)
                .on(productCategory.product.eq(products))
            .innerJoin(category).
                on(category.eq(productCategory.category));
    }

    @Override
    public List<ProductCardQueryResult> findOnSaleNewProducts(LocalDate from, int limit) {
        return baseQuery()
                        .where(
                            products.createdAt.goe(from.atStartOfDay()),
                            products.productStatus.eq(ProductStatus.SALE)
                        )
                        .orderBy(products.createdAt.desc())
                        .limit(limit)
                        .fetch();
    }

    @Override
    public List<ProductCardQueryResult> findOnSaleCategoryProducts(Long categoryId, int limit) {
        return baseQuery()
                        .where(
                            category.id.eq(categoryId),
                            products.productStatus.eq(ProductStatus.SALE)
                        )
                        .orderBy(products.createdAt.desc())
                        .limit(limit)
                        .fetch();
    }

    @Override
    public Page<@NonNull ProductCardQueryResult> findOnSaleProductsByCategory(Category selectCategory,
        ProductSortType sort, SortDirection direction, Pageable pageable) {

        BooleanBuilder condition = new BooleanBuilder();
        if (Objects.nonNull(selectCategory)) {
            condition.and(productCategory.category.eq(selectCategory));
        }
        condition.and(products.productStatus.eq(ProductStatus.SALE));

        List<ProductCardQueryResult> content = baseQuery()
            .where(condition)
            .orderBy(resolveSort(sort, direction))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

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
            case POPULAR -> null;
        };
    }
}
