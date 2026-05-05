package com.daebbang.daebbangcore.domain.point.repository.dsl.impl;

import static com.daebbang.daebbangcore.domain.point.entity.QPointPolicy.pointPolicy;
import static com.daebbang.daebbangcore.domain.point.entity.QPoints.points;
import static com.daebbang.daebbangcore.domain.point.entity.QUserPointHistory.userPointHistory;

import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.repository.dsl.UserPointHistoryCustomRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPointHistoryCustomRepositoryImpl implements UserPointHistoryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<@NonNull UserPointHistory> findPageByUserId(Long userId, Pageable pageable) {
        List<UserPointHistory> content = queryFactory
            .selectFrom(userPointHistory)
            .leftJoin(userPointHistory.pointPolicy, pointPolicy).fetchJoin()
            .innerJoin(userPointHistory.points, points)
            .where(points.user.id.eq(userId))
            .orderBy(userPointHistory.createdAt.desc(), userPointHistory.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(userPointHistory.count())
            .from(userPointHistory)
            .innerJoin(userPointHistory.points, points)
            .where(points.user.id.eq(userId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
