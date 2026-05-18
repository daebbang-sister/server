package com.daebbang.daebbangcore.domain.order.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.repository.dsl.OrderCustomRepository;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import static com.daebbang.daebbangcore.domain.order.entity.QOrderDetails.orderDetails;
import static com.daebbang.daebbangcore.domain.order.entity.QOrders.orders;
import static com.daebbang.daebbangcore.domain.product.entity.QProductDetails.productDetails;
import static com.daebbang.daebbangcore.domain.product.entity.QProducts.products;
import static com.daebbang.daebbangcore.domain.user.entity.QUsers.users;

@Repository
@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private static final Map<String, ComparableExpressionBase<?>> ORDER_SORTS = Map.of(
        "createdAt", orders.createdAt
    );

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<@NonNull Orders> findOrdersByUserIdAndDateRange(Long userId, LocalDateTime start,
                                                       LocalDateTime end, Pageable pageable) {
        List<Long> ids = queryFactory
            .select(orders.id)
            .from(orders)
            .where(
                orders.user.id.eq(userId),
                orders.createdAt.between(start, end)
            )
            .orderBy(toOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (ids.isEmpty()) {
            JPAQuery<Long> countQuery = queryFactory
                .select(orders.count())
                .from(orders)
                .where(
                    orders.user.id.eq(userId),
                    orders.createdAt.between(start, end)
                );
            return PageableExecutionUtils.getPage(List.of(), pageable, countQuery::fetchOne);
        }

        List<Orders> content = queryFactory
            .selectFrom(orders)
            .leftJoin(orders.orderList, orderDetails).fetchJoin()
            .leftJoin(orderDetails.productDetail, productDetails).fetchJoin()
            .leftJoin(productDetails.product, products).fetchJoin()
            .where(orders.id.in(ids))
            .orderBy(toOrderSpecifiers(pageable.getSort()))
            .distinct()
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(orders.count())
            .from(orders)
            .where(
                orders.user.id.eq(userId),
                orders.createdAt.between(start, end)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        if (sort.isUnsorted()) {
            return new OrderSpecifier[]{orders.createdAt.desc(), orders.id.desc()};
        }
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        for (Sort.Order o : sort) {
            ComparableExpressionBase<?> path = ORDER_SORTS.get(o.getProperty());
            if (path == null) continue;
            Order direction = o.isAscending() ? Order.ASC : Order.DESC;
            specifiers.add(new OrderSpecifier(direction, path));
        }
        if (specifiers.isEmpty()) {
            return new OrderSpecifier[]{orders.createdAt.desc(), orders.id.desc()};
        }
        specifiers.add(new OrderSpecifier(Order.DESC, orders.id));
        return specifiers.toArray(new OrderSpecifier[0]);
    }

    @Override
    public Optional<Orders> findOrderDetailByOrderNumberAndUserId(String orderNumber, Long userId) {
        Orders result = queryFactory
            .selectFrom(orders)
            .join(orders.user, users).fetchJoin()
            .leftJoin(orders.orderList, orderDetails).fetchJoin()
            .leftJoin(orderDetails.productDetail, productDetails).fetchJoin()
            .leftJoin(productDetails.product, products).fetchJoin()
            .where(
                orders.orderNumber.eq(orderNumber),
                orders.user.id.eq(userId)
            )
            .distinct()
            .fetchOne();

        return Optional.ofNullable(result);
    }

    private static final List<OrderStatus> DELIVERY_STATUSES = List.of(
        OrderStatus.PAID, OrderStatus.PREPARING_DELIVERY,
        OrderStatus.IN_DELIVERY, OrderStatus.DELIVERED
    );

    @Override
    public OrderStatusCountResult countOrderStatusByUserIdAndDateRange(Long userId,
                                                                       LocalDateTime start,
                                                                       LocalDateTime end) {
        Map<OrderStatus, Long> countMap = queryFactory
            .select(orders.orderStatus, orders.count())
            .from(orders)
            .where(
                orders.user.id.eq(userId),
                orders.createdAt.between(start, end),
                orders.orderStatus.in(DELIVERY_STATUSES)
            )
            .groupBy(orders.orderStatus)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> Objects.requireNonNull(tuple.get(orders.orderStatus)),
                tuple -> Objects.requireNonNull(tuple.get(orders.count()))
            ));

        return new OrderStatusCountResult(
            countMap.getOrDefault(OrderStatus.PAID, 0L),
            countMap.getOrDefault(OrderStatus.PREPARING_DELIVERY, 0L),
            countMap.getOrDefault(OrderStatus.IN_DELIVERY, 0L),
            countMap.getOrDefault(OrderStatus.DELIVERED, 0L)
        );
    }
}
