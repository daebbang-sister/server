package com.daebbang.daebbangcore.domain.review.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult;
import com.daebbang.daebbangcore.domain.review.entity.QReview;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.repository.dsl.ReviewCustomRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {

    private final JPAQueryFactory queryFactory;

    private static final QReview review = QReview.review;

    @Override
    public Page<@NonNull Review> findActiveReviewsByProductId(Long productId, Pageable pageable) {
        List<Review> content = queryFactory
            .selectFrom(review)
            .leftJoin(review.user).fetchJoin()
            .where(
                review.product.id.eq(productId),
                review.deletedAt.isNull()
            )
            .orderBy(review.createdAt.desc(), review.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .where(
                review.product.id.eq(productId),
                review.deletedAt.isNull()
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    @Override
    public Page<@NonNull Review> findActiveReviewsByUserId(Long userId, Pageable pageable) {
        List<Review> content = queryFactory
            .selectFrom(review)
            .leftJoin(review.product).fetchJoin()
            .where(
                review.user.id.eq(userId),
                review.deletedAt.isNull()
            )
            .orderBy(review.createdAt.desc(), review.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .where(
                review.user.id.eq(userId),
                review.deletedAt.isNull()
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    @Override
    public Page<@NonNull Review> findActiveReviewsForAdmin(Pageable pageable) {
        List<Review> content = queryFactory
            .selectFrom(review)
            .where(review.deletedAt.isNull())
            .orderBy(review.createdAt.desc(), review.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(review.count())
            .from(review)
            .where(review.deletedAt.isNull())
            .fetchOne();

        return new PageImpl<>(content, pageable, Objects.nonNull(total) ? total : 0L);
    }

    @Override
    public ReviewStatsResult getReviewStats(Long productId) {
        List<Tuple> ratingData = queryFactory
            .select(review.rating, review.count())
            .from(review)
            .where(
                review.product.id.eq(productId),
                review.deletedAt.isNull()
            )
            .groupBy(review.rating)
            .fetch();

        Map<Integer, Long> ratingCounts = new java.util.TreeMap<>();
        for (int score = 1; score <= 5; score++) {
            ratingCounts.put(score, 0L);
        }
        for (Tuple t : ratingData) {
            Integer score = t.get(review.rating);
            Long count = t.get(review.count());
            if (score != null) {
                ratingCounts.put(score, Objects.requireNonNullElse(count, 0L));
            }
        }

        long totalCount = ratingCounts.values().stream().mapToLong(Long::longValue).sum();
        double averageRating = totalCount == 0 ? 0.0 :
            ratingCounts.entrySet().stream()
                .mapToDouble(e -> e.getKey() * e.getValue())
                .sum() / totalCount;

        return new ReviewStatsResult(totalCount, Math.round(averageRating * 10.0) / 10.0, ratingCounts);
    }
}
