package com.daebbang.daebbangcore.domain.order.repository.dsl.impl;

import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.repository.dsl.OrderCustomRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.daebbang.daebbangcore.domain.order.entity.QOrderDetails.orderDetails;
import static com.daebbang.daebbangcore.domain.order.entity.QOrders.orders;
import static com.daebbang.daebbangcore.domain.product.entity.QProductDetails.productDetails;
import static com.daebbang.daebbangcore.domain.product.entity.QProducts.products;

@Repository
@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 2단계 쿼리로 컬렉션 fetch join + 페이징 충돌 방지.
     * 1단계: 주문 ID만 페이징 조회 (row 뻥튀기 없음)
     * 2단계: ID로 LEFT JOIN FETCH (orderList 없어도 주문은 포함)
     */
    @Override
    public Page<Orders> findOrdersByUserIdAndDateRange(Long userId, LocalDateTime start,
                                                       LocalDateTime end, Pageable pageable) {
        // 1단계: ID 페이징
        List<Long> ids = queryFactory
            .select(orders.id)
            .from(orders)
            .where(
                orders.user.id.eq(userId),
                orders.createdAt.between(start, end)
            )
            .orderBy(orders.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        Long total = queryFactory
            .select(orders.count())
            .from(orders)
            .where(
                orders.user.id.eq(userId),
                orders.createdAt.between(start, end)
            )
            .fetchOne();

        // 2단계: ID로 fetch join (left join → orderList 비어도 주문 포함)
        List<Orders> content = queryFactory
            .selectFrom(orders)
            .leftJoin(orders.orderList, orderDetails).fetchJoin()
            .leftJoin(orderDetails.productDetail, productDetails).fetchJoin()
            .leftJoin(productDetails.product, products).fetchJoin()
            .where(orders.id.in(ids))
            .orderBy(orders.createdAt.desc())
            .distinct()
            .fetch();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Optional<Orders> findOrderDetailByOrderNumberAndUserId(String orderNumber, Long userId) {
        Orders result = queryFactory
            .selectFrom(orders)
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

    @Override
    public OrderStatusCountResult countOrderStatusByUserIdAndDateRange(Long userId,
                                                                       LocalDateTime start,
                                                                       LocalDateTime end) {
        Map<OrderStatus, Long> countMap = queryFactory
            .select(orders.orderStatus, orders.count())
            .from(orders)
            .where(
                orders.user.id.eq(userId),
                orders.createdAt.between(start, end)
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
