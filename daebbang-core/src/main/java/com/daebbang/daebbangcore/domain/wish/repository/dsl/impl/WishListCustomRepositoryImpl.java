package com.daebbang.daebbangcore.domain.wish.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;
import com.daebbang.daebbangcore.domain.wish.repository.dsl.WishListCustomRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.daebbang.daebbangcore.domain.wish.entity.QWishList.wishList;
import static com.daebbang.daebbangcore.domain.product.entity.QProducts.products;

@Repository
@RequiredArgsConstructor
public class WishListCustomRepositoryImpl implements WishListCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<@NonNull WishListQueryResult> findWishListPageByUserId(Long userId, Pageable pageable) {

        List<WishListQueryResult> content = queryFactory
            .select(Projections.constructor(WishListQueryResult.class,
                wishList.id,
                products.id,
                products.mainImageUrl,
                products.productName,
                products.originalPrice,
                products.discountType,
                products.discountRate,
                products.discountStartDate,
                products.discountEndDate
            ))
            .from(wishList)
            .innerJoin(wishList.product, products)
            .where(wishList.user.id.eq(userId))
            .orderBy(wishList.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(wishList.count())
            .from(wishList)
            .where(wishList.user.id.eq(userId))
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }
}
