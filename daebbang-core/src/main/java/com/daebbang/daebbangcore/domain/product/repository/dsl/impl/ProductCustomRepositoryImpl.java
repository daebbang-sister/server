package com.daebbang.daebbangcore.domain.product.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import com.daebbang.daebbangcore.domain.product.repository.dsl.ProductCustomRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    public List<ProductCardQueryResult> findNewProductsOnSale(LocalDate from, int limit) {
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
    public List<ProductCardQueryResult> findCategoryProductsOnSale(Long categoryId, int limit) {
        return baseQuery()
                        .where(
                            category.id.eq(categoryId),
                            products.productStatus.eq(ProductStatus.SALE)
                        )
                        .orderBy(products.createdAt.desc())
                        .limit(limit)
                        .fetch();
    }
}
