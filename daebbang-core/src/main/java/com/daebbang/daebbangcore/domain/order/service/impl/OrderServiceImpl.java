package com.daebbang.daebbangcore.domain.order.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import com.daebbang.daebbangcommon.error.PointErrorCode;
import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.service.PointService;
import com.daebbang.daebbangcore.domain.order.command.OrderCancelCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderConfirmCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderItemCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderPartialCancelCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import com.daebbang.daebbangcore.domain.order.dto.OrderFullDetailResult;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.dto.OrderSummaryResult;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.entity.Payment;
import com.daebbang.daebbangcore.domain.order.event.StockInvalidateEvent;
import com.daebbang.daebbangcore.domain.order.repository.OrdersRepository;
import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.order.service.OrderService;
import com.daebbang.daebbangcore.domain.order.service.PaymentService;
import com.daebbang.daebbangcore.domain.order.service.StockCacheService;
import com.daebbang.daebbangcore.domain.order.session.OrderSession;
import com.daebbang.daebbangcore.domain.order.session.OrderSessionItem;
import com.daebbang.daebbangcore.domain.order.session.OrderSessionRedisRepository;
import com.daebbang.daebbangcore.domain.order.session.OrderStockReserveRedisRepository;
import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.product.service.ProductDetailsService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.toss.TossPaymentClient;
import com.daebbang.daebbangcore.infra.toss.dto.TossPaymentResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private static final int FREE_SHIPPING_THRESHOLD = 50_000;
    private static final int DEFAULT_SHIPPING_FEE = 3_000;
    private static final int MIN_PAYMENT_AMOUNT_FOR_POINT_USE = 30_000;
    private static final String CANCEL_IDEMPOTENCY_PREFIX = "order:cancel:idempotency:";
    private static final Duration CANCEL_IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final UserService userService;
    private final AddressService addressService;
    private final ProductDetailsService productDetailsService;
    private final OrdersRepository ordersRepository;
    private final PaymentService paymentService;
    private final PointService pointService;
    private final StockCacheService stockCacheService;
    private final OrderSessionRedisRepository orderSessionRedisRepository;
    private final TossPaymentClient tossPaymentClient;
    private final RedissonClient redissonClient;
    private final OrderStockReserveRedisRepository stockReserveRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;
    private final StringRedisTemplate stringRedisTemplate;

    private TransactionTemplate readTx() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setReadOnly(true);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx;
    }

    private TransactionTemplate writeTx() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx;
    }

    private record CancelPrecheck(String paymentKey, int cancelableAmount) {}
    private record PartialCancelPrecheck(String paymentKey, int cancelAmount, List<Long> targetDetailIds) {}

    @Override
    public OrderPrepareResponse prepare(OrderPrepareCommand command) {
        Users user = userService.getUserById(command.userId());

        if (command.isAddToAddressBook()) {
            addressService.save(user, new AddressCommand(
                command.receiver(), command.receiverPhoneNumber(),
                command.addressAlias(), command.zipCode(), command.address(), command.detailAddress(),
                command.isDefaultAddress()
            ));
        }

        List<OrderSessionItem> sessionItems = new ArrayList<>();
        Map<Long, Integer> decreasedStocks = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        boolean reserved = false;
        String orderNumber = null;
        int paymentAmount;
        try {
            for (OrderItemCommand item : command.items()) {
                boolean acquired = false;
                RLock lock = redissonClient.getLock("stock:lock:" + item.productDetailId());
                try {
                    acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
                    if (!acquired) {
                        throw new BusinessException(OrderErrorCode.STOCK_LOCK_FAILED);
                    }

                    stockCacheService.decreaseStock(item.productDetailId(), item.quantity());
                    decreasedStocks.merge(item.productDetailId(), item.quantity(), Integer::sum);

                    ProductDetails pd = productDetailsService.getProductDetailsById(item.productDetailId());
                    Products product = pd.getProduct();
                    int discountRate = resolveDiscountRate(product, today);
                    int originalPrice = product.getOriginalPrice();
                    int discountPrice = (int) (originalPrice * (1 - discountRate / 100.0)) * item.quantity();

                    sessionItems.add(OrderSessionItem.builder()
                        .productDetailId(item.productDetailId())
                        .quantity(item.quantity())
                        .originalPrice(originalPrice)
                        .discountRate(discountRate)
                        .discountPrice(discountPrice)
                        .build());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(OrderErrorCode.STOCK_LOCK_FAILED);
                } finally {
                    if (acquired) lock.unlock();
                }
            }

            int totalOriginalAmount = sessionItems.stream()
                .mapToInt(i -> i.getOriginalPrice() * i.getQuantity())
                .sum();
            int totalSellingAmount = sessionItems.stream()
                .mapToInt(OrderSessionItem::getDiscountPrice)
                .sum();

            int expectedShippingFee = totalSellingAmount >= FREE_SHIPPING_THRESHOLD ? 0 : DEFAULT_SHIPPING_FEE;
            if (command.shippingFee() != expectedShippingFee) {
                throw new BusinessException(OrderErrorCode.ORDER_SHIPPING_FEE_INVALID);
            }
            int shippingFee = expectedShippingFee;

            if (command.usedPoint() > totalSellingAmount + shippingFee) {
                throw new BusinessException(OrderErrorCode.POINT_EXCEEDS_PAYMENT);
            }
            if (command.usedPoint() > 0) {
                if (totalSellingAmount + shippingFee < MIN_PAYMENT_AMOUNT_FOR_POINT_USE) {
                    throw new BusinessException(PointErrorCode.POINT_USE_BELOW_MIN_ORDER);
                }
                PointBalanceResult balance = pointService.getBalance(command.userId());
                if (balance.currentAmount() < command.usedPoint()) {
                    throw new BusinessException(PointErrorCode.POINT_INSUFFICIENT_BALANCE);
                }
            }
            paymentAmount = totalSellingAmount + shippingFee - command.usedPoint();

            orderNumber = generateOrderNumber();

            OrderSession session = OrderSession.builder()
                .orderNumber(orderNumber)
                .userId(user.getId())
                .items(sessionItems)
                .usedPoint(command.usedPoint())
                .shippingFee(shippingFee)
                .totalOriginalAmount(totalOriginalAmount)
                .totalSellingAmount(totalSellingAmount)
                .paymentAmount(paymentAmount)
                .receiver(command.receiver())
                .receiverPhoneNumber(command.receiverPhoneNumber())
                .zipCode(command.zipCode())
                .address(command.address())
                .detailAddress(command.detailAddress())
                .orderNote(command.orderNote())
                .build();

            orderSessionRedisRepository.save(orderNumber, session);
            stockReserveRedisRepository.save(orderNumber, sessionItems);
            reserved = true;
        } finally {
            if (!reserved) {
                if (orderNumber != null) {
                    try {
                        orderSessionRedisRepository.delete(orderNumber);
                    } catch (Exception e) {
                        log.warn("[Order] prepare 보상 - orderSession 삭제 실패: orderNumber={}", orderNumber, e);
                    }
                    try {
                        stockReserveRedisRepository.delete(orderNumber);
                    } catch (Exception e) {
                        log.warn("[Order] prepare 보상 - stockReserve 삭제 실패: orderNumber={}", orderNumber, e);
                    }
                }
                decreasedStocks.forEach(stockCacheService::restoreStock);
            }
        }

        log.info("[Order] prepare 완료 - orderNumber: {}, userId: {}", orderNumber, user.getId());
        return new OrderPrepareResponse(orderNumber, paymentAmount);
    }

    @Override
    @Transactional
    public void confirm(OrderConfirmCommand command) {
        OrderSession session = orderSessionRedisRepository.findByOrderNumber(command.orderNumber())
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (!session.getUserId().equals(command.userId())) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_MISMATCH);
        }

        if (session.getPaymentAmount() != command.amount()) {
            throw new BusinessException(OrderErrorCode.ORDER_AMOUNT_MISMATCH);
        }

        RLock confirmLock = redissonClient.getLock("order:confirm:lock:" + command.orderNumber());
        boolean acquired = false;
        try {
            acquired = confirmLock.tryLock(3, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
            }

            if (ordersRepository.existsByOrderNumber(command.orderNumber())) {
                throw new BusinessException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
            }

            TossPaymentResponse tossResponse = tossPaymentClient.confirm(
                command.orderNumber(), command.paymentKey(), command.amount()
            );

            try {
                for (OrderSessionItem item : session.getItems()) {
                    int updated = productDetailsService.decreaseStock(
                        item.getProductDetailId(), item.getQuantity()
                    );
                    if (updated == 0) {
                        log.error("[Order] DB 재고 부족 - productDetailId: {}", item.getProductDetailId());
                        throw new BusinessException(OrderErrorCode.OUT_OF_STOCK);
                    }
                }

                Users user = userService.getUserById(session.getUserId());

                Orders order = Orders.create(
                    user,
                    session.getOrderNumber(),
                    session.getUsedPoint(),
                    session.getShippingFee(),
                    session.getTotalOriginalAmount(),
                    session.getTotalSellingAmount()
                );

                for (OrderSessionItem item : session.getItems()) {
                    ProductDetails pd = productDetailsService.getProductDetailsById(item.getProductDetailId());
                    order.addDetail(OrderDetails.create(
                        pd, item.getQuantity(), item.getOriginalPrice(), item.getDiscountRate()
                    ));
                }

                if (tossResponse.isVirtualAccount()) {
                    order.waitDeposit();
                } else {
                    order.pay();
                }
                order.update();
                ordersRepository.save(order);

                if (session.getUsedPoint() > 0) {
                    pointService.usePointForPayment(
                        session.getUserId(),
                        order.getId(),
                        session.getUsedPoint(),
                        session.getTotalSellingAmount() + session.getShippingFee()
                    );
                }

                LocalDateTime approvedAt = tossResponse.getApprovedAt() != null
                    ? OffsetDateTime.parse(tossResponse.getApprovedAt()).toLocalDateTime()
                    : LocalDateTime.now();

                Payment payment = Payment.create(
                    order,
                    tossResponse.getPaymentKey(),
                    tossResponse.getCurrency(),
                    tossResponse.getMethod(),
                    tossResponse.getTotalAmount(),
                    OffsetDateTime.parse(tossResponse.getRequestedAt()).toLocalDateTime(),
                    approvedAt
                );
                payment.update();
                paymentService.save(payment);

                log.info("[Order] confirm 완료 - orderNumber: {}, method: {}",
                    command.orderNumber(), tossResponse.getMethod());

            } catch (Exception e) {
                log.error("[Order] DB 처리 실패 - Toss 취소 시도 - orderNumber: {}", command.orderNumber(), e);
                try {
                    tossPaymentClient.cancel(command.paymentKey(), "시스템 오류로 인한 자동 취소", UUID.randomUUID().toString());
                } catch (Exception cancelEx) {
                    log.error("[Order] Toss 취소 실패 - 수동 취소 필요 - paymentKey: {}", command.paymentKey(), cancelEx);
                }
                session.getItems().forEach(i ->
                    stockCacheService.restoreStock(i.getProductDetailId(), i.getQuantity()));
                stockReserveRedisRepository.delete(command.orderNumber());
                if (e instanceof BusinessException) throw e;
                throw new BusinessException(OrderErrorCode.PAYMENT_CONFIRMATION_FAILED);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
        } finally {
            if (acquired) confirmLock.unlock();
        }

        // 11. Redis 세션 삭제 & 예약 키 삭제 & 재고 캐시 무효화
        orderSessionRedisRepository.delete(command.orderNumber());
        stockReserveRedisRepository.delete(command.orderNumber());
        session.getItems().forEach(i -> stockCacheService.invalidateStock(i.getProductDetailId()));
    }

    @Override
    public void cancel(OrderCancelCommand command) {
        withCancelLock(command.orderNumber(), () -> {
            // [Phase 1] read tx: 검증 + 필요 데이터 수집 후 커넥션 반환
            CancelPrecheck precheck = Objects.requireNonNull(readTx().execute(status -> {
                Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                        command.orderNumber(), command.userId())
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

                if (order.getOrderStatus() != OrderStatus.PAID
                    && order.getOrderStatus() != OrderStatus.WAITING_DEPOSIT
                    && order.getOrderStatus() != OrderStatus.PREPARING_DELIVERY) {
                    throw new BusinessException(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
                }

                Payment payment = paymentService.findByOrder(order);
                int cancelableAmount = payment.getTotalAmount() - payment.getTotalCancelAmount();
                if (cancelableAmount <= 0) {
                    throw new BusinessException(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
                }

                return new CancelPrecheck(payment.getPaymentKey(), cancelableAmount);
            }));

            // [Phase 2] Toss API 호출 — DB 커넥션 미보유
            String idempotencyKey = getOrCreateCancelIdempotencyKey(command.orderNumber());
            tossPaymentClient.cancel(precheck.paymentKey(), command.cancelReason(), idempotencyKey);

            // [Phase 3] write tx: DB 상태 반영
            writeTx().execute(status -> {
                Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                        command.orderNumber(), command.userId())
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
                Payment payment = paymentService.findByOrder(order);

                List<OrderDetails> cancelTargets = order.getOrderList().stream()
                    .filter(d -> d.getStatus() == OrderDetailStatus.NORMAL)
                    .toList();

                for (OrderDetails detail : cancelTargets) {
                    detail.cancelRequest();
                    detail.cancel();
                }
                order.cancel();
                paymentService.recordCancel(payment, command.cancelReason(), precheck.cancelableAmount());
                restoreStockAndPublishEvent(cancelTargets);

                if (order.getUsedPoint() > 0) {
                    pointService.refundUsedPoint(command.userId(), order.getId(), order.getUsedPoint());
                }

                log.info("[Order] 전체 취소 완료 - orderNumber: {}, userId: {}",
                    command.orderNumber(), command.userId());
                return null;
            });

            deleteCancelIdempotencyKey(command.orderNumber());
        });
    }

    @Override
    public void cancelPartial(OrderPartialCancelCommand command) {
        withCancelLock(command.orderNumber(), () -> {
            // [Phase 1] read tx: 검증 + 필요 데이터 수집
            PartialCancelPrecheck precheck = Objects.requireNonNull(readTx().execute(status -> {
                Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                        command.orderNumber(), command.userId())
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

                if (order.getOrderStatus() != OrderStatus.PAID
                    && order.getOrderStatus() != OrderStatus.PREPARING_DELIVERY) {
                    throw new BusinessException(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED);
                }

                List<Long> targetIds = command.orderDetailIds();
                List<OrderDetails> targetDetails = order.getOrderList().stream()
                    .filter(d -> targetIds.contains(d.getId()))
                    .toList();

                if (targetDetails.size() != targetIds.size()) {
                    throw new BusinessException(OrderErrorCode.ORDER_PARTIAL_CANCEL_INVALID);
                }
                if (targetDetails.stream().anyMatch(d -> d.getStatus() != OrderDetailStatus.NORMAL)) {
                    throw new BusinessException(OrderErrorCode.ORDER_PARTIAL_CANCEL_INVALID);
                }

                int cancelAmount = targetDetails.stream()
                    .mapToInt(OrderDetails::getDiscountPrice)
                    .sum();

                // 모든 항목 취소 시 배송비도 환불
                boolean allWillBeCancelled = order.getOrderList().stream()
                    .allMatch(d -> targetIds.contains(d.getId()) || d.getStatus() == OrderDetailStatus.CANCELLED);
                if (allWillBeCancelled) {
                    cancelAmount += order.getShippingFee();
                }

                Payment payment = paymentService.findByOrder(order);
                int remainingAmount = payment.getTotalAmount() - payment.getTotalCancelAmount();
                if (cancelAmount > remainingAmount || cancelAmount <= 0) {
                    throw new BusinessException(OrderErrorCode.ORDER_PARTIAL_CANCEL_INVALID);
                }

                List<Long> targetDetailIds = targetDetails.stream().map(OrderDetails::getId).toList();
                return new PartialCancelPrecheck(payment.getPaymentKey(), cancelAmount, targetDetailIds);
            }));

            // [Phase 2] Toss API 호출 — DB 커넥션 미보유
            String idempotencyKey = getOrCreateCancelIdempotencyKey(command.orderNumber());
            tossPaymentClient.cancel(precheck.paymentKey(), command.cancelReason(), precheck.cancelAmount(), idempotencyKey);

            // [Phase 3] write tx: DB 상태 반영
            writeTx().execute(status -> {
                Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                        command.orderNumber(), command.userId())
                    .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
                Payment payment = paymentService.findByOrder(order);

                List<OrderDetails> targetDetails = order.getOrderList().stream()
                    .filter(d -> precheck.targetDetailIds().contains(d.getId()))
                    .toList();

                for (OrderDetails detail : targetDetails) {
                    detail.cancelRequest();
                    detail.cancel();
                }

                boolean allCancelled = order.getOrderList().stream()
                    .allMatch(d -> d.getStatus() == OrderDetailStatus.CANCELLED);
                if (allCancelled) {
                    order.cancel();
                }

                paymentService.recordCancel(payment, command.cancelReason(), precheck.cancelAmount());
                restoreStockAndPublishEvent(targetDetails);

                if (allCancelled && order.getUsedPoint() > 0) {
                    pointService.refundUsedPoint(command.userId(), order.getId(), order.getUsedPoint());
                }

                log.info("[Order] 부분 취소 완료 - orderNumber: {}, userId: {}, 취소금액: {}",
                    command.orderNumber(), command.userId(), precheck.cancelAmount());
                return null;
            });

            deleteCancelIdempotencyKey(command.orderNumber());
        });
    }

    @Override
    @Transactional
    public void complete(Long userId, String orderNumber) {
        Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(orderNumber, userId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(OrderErrorCode.ORDER_NOT_DELIVERED);
        }

        order.complete();
        order.update();

        pointService.awardPurchasePoint(userId, order.getId(), order.getPaymentAmount());

        log.info("[Order] 구매 확정 완료 - orderNumber: {}, userId: {}", orderNumber, userId);
    }

    @Override
    public Page<@NonNull OrderSummaryResult> getOrderList(Long userId, LocalDateTime start,
                                                  LocalDateTime end, Pageable pageable) {
        return ordersRepository.findOrdersByUserIdAndDateRange(userId, start, end, pageable)
            .map(this::toSummaryResult);
    }

    @Override
    public OrderFullDetailResult getOrderDetail(Long userId, String orderNumber) {
        Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(orderNumber, userId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return toFullDetailResult(order);
    }

    @Override
    public OrderStatusCountResult getOrderStatusCount(Long userId, LocalDateTime start,
                                                       LocalDateTime end) {
        return ordersRepository.countOrderStatusByUserIdAndDateRange(userId, start, end);
    }

    private OrderSummaryResult toSummaryResult(Orders order) {
        List<OrderDetails> details = order.getOrderList();
        OrderDetails first = details.isEmpty() ? null : details.get(0);
        int totalQuantity = details.stream().mapToInt(OrderDetails::getQuantity).sum();
        int representativeUnitPrice = (first != null && first.getQuantity() > 0)
            ? first.getDiscountPrice() / first.getQuantity()
            : 0;

        return new OrderSummaryResult(
            order.getId(),
            order.getOrderNumber(),
            order.getOrderStatus(),
            order.getCreatedAt(),
            order.getPaymentAmount(),
            totalQuantity,
            first != null ? first.getProductDetail().getProduct().getProductName() : null,
            first != null ? first.getProductDetail().getProduct().getMainImageUrl() : null,
            first != null ? first.getProductDetail().getColor() : null,
            first != null ? first.getProductDetail().getSize() : null,
            representativeUnitPrice,
            first != null ? first.getQuantity() : 0
        );
    }

    private OrderFullDetailResult toFullDetailResult(Orders order) {
        List<OrderFullDetailResult.OrderDetailItem> items = order.getOrderList().stream()
            .map(d -> new OrderFullDetailResult.OrderDetailItem(
                d.getId(),
                d.getProductDetail().getId(),
                d.getProductDetail().getProduct().getProductName(),
                d.getProductDetail().getProduct().getMainImageUrl(),
                d.getProductDetail().getColor(),
                d.getProductDetail().getColorCode(),
                d.getProductDetail().getSize(),
                d.getQuantity(),
                d.getOriginalPrice(),
                d.getDiscountRate(),
                d.getDiscountPrice(),
                d.getStatus()
            ))
            .toList();

        int expectedPoint = pointService.calculateExpectedPurchasePoint(order.getPaymentAmount());
        int earnedPoint = pointService.findEarnedPointByOrder(order.getId());

        return new OrderFullDetailResult(
            order.getOrderNumber(),
            order.getOrderStatus(),
            order.getCreatedAt(),
            order.getTotalOriginalAmount(),
            order.getTotalSellingAmount(),
            order.getShippingFee(),
            order.getUsedPoint(),
            order.getPaymentAmount(),
            expectedPoint,
            earnedPoint,
            items
        );
    }

    private int resolveDiscountRate(Products product, LocalDate today) {
        if (product.getDiscountRate() == null) return 0;
        return switch (product.getDiscountType()) {
            case ALWAYS -> product.getDiscountRate();
            case PERIOD -> {
                LocalDate start = product.getDiscountStartDate();
                LocalDate end = product.getDiscountEndDate();
                if (start != null && end != null && !today.isBefore(start) && !today.isAfter(end)) {
                    yield product.getDiscountRate();
                }
                yield 0;
            }
            default -> 0;
        };
    }

    private void withCancelLock(String orderNumber, Runnable action) {
        RLock cancelLock = redissonClient.getLock("order:cancel:lock:" + orderNumber);
        boolean acquired = false;
        try {
            acquired = cancelLock.tryLock(3, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
            }
            action.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(OrderErrorCode.ORDER_ALREADY_PROCESSED);
        } finally {
            if (acquired) cancelLock.unlock();
        }
    }

    private void restoreStockAndPublishEvent(List<OrderDetails> details) {
        for (OrderDetails detail : details) {
            int updated = productDetailsService.increaseStock(
                detail.getProductDetail().getId(), detail.getQuantity()
            );
            if (updated != 1) {
                log.error("[Order] 재고 복원 실패 - productDetailId: {}", detail.getProductDetail().getId());
                throw new BusinessException(OrderErrorCode.STOCK_RESTORE_FAILED);
            }
        }
        List<Long> productDetailIds = details.stream()
            .map(d -> d.getProductDetail().getId())
            .toList();
        eventPublisher.publishEvent(new StockInvalidateEvent(productDetailIds));
    }

    private String getOrCreateCancelIdempotencyKey(String orderNumber) {
        String redisKey = CANCEL_IDEMPOTENCY_PREFIX + orderNumber;
        String existing = stringRedisTemplate.opsForValue().get(redisKey);
        if (existing != null) {
            return existing;
        }
        String newKey = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(redisKey, newKey, CANCEL_IDEMPOTENCY_TTL);
        return newKey;
    }

    private void deleteCancelIdempotencyKey(String orderNumber) {
        stringRedisTemplate.delete(CANCEL_IDEMPOTENCY_PREFIX + orderNumber);
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return date + "-" + random;
    }
}
