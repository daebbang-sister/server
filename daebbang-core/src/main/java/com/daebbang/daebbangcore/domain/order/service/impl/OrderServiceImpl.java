package com.daebbang.daebbangcore.domain.order.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final UserService userService;
    private final ProductDetailsService productDetailsService;
    private final OrdersRepository ordersRepository;
    private final PaymentService paymentService;
    private final StockCacheService stockCacheService;
    private final OrderSessionRedisRepository orderSessionRedisRepository;
    private final TossPaymentClient tossPaymentClient;
    private final RedissonClient redissonClient;
    private final OrderStockReserveRedisRepository stockReserveRedisRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PlatformTransactionManager transactionManager;

    // read tx: 검증 단계에서 커넥션 조기 반환
    private TransactionTemplate readTx() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setReadOnly(true);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return tx;
    }

    // write tx: Toss 호출 후 DB 반영
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

        List<OrderSessionItem> sessionItems = new ArrayList<>();
        Map<Long, Integer> decreasedStocks = new LinkedHashMap<>();

        try {
            for (OrderItemCommand item : command.items()) {
                boolean acquired = false;
                RLock lock = redissonClient.getLock("stock:lock:" + item.productDetailId());
                try {
                    acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
                    if (!acquired) {
                        throw new BusinessException(UserErrorCode.STOCK_LOCK_FAILED);
                    }

                    stockCacheService.decreaseStock(item.productDetailId(), item.quantity());
                    decreasedStocks.merge(item.productDetailId(), item.quantity(), Integer::sum);

                    ProductDetails pd = productDetailsService.getProductDetailsById(item.productDetailId());
                    Products product = pd.getProduct();

                    int discountRate = product.getDiscountRate() != null ? product.getDiscountRate() : 0;
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
                    throw new BusinessException(UserErrorCode.STOCK_LOCK_FAILED);
                } finally {
                    if (acquired) lock.unlock();
                }
            }
        } catch (BusinessException e) {
            decreasedStocks.forEach(stockCacheService::restoreStock);
            throw e;
        }

        int totalOriginalAmount = sessionItems.stream()
            .mapToInt(i -> i.getOriginalPrice() * i.getQuantity())
            .sum();
        int totalSellingAmount = sessionItems.stream()
            .mapToInt(OrderSessionItem::getDiscountPrice)
            .sum();
        int shippingFee = totalSellingAmount >= FREE_SHIPPING_THRESHOLD ? 0 : DEFAULT_SHIPPING_FEE;
        int paymentAmount = totalSellingAmount + shippingFee - command.usedPoint();

        String orderNumber = generateOrderNumber();

        OrderSession session = OrderSession.builder()
            .orderNumber(orderNumber)
            .userId(user.getId())
            .items(sessionItems)
            .usedPoint(command.usedPoint())
            .shippingFee(shippingFee)
            .totalOriginalAmount(totalOriginalAmount)
            .totalSellingAmount(totalSellingAmount)
            .paymentAmount(paymentAmount)
            .build();

        orderSessionRedisRepository.save(orderNumber, session);
        stockReserveRedisRepository.save(orderNumber, sessionItems);
        log.info("[Order] prepare 완료 - orderNumber: {}, userId: {}", orderNumber, user.getId());

        return new OrderPrepareResponse(orderNumber, paymentAmount);
    }

    @Override
    @Transactional
    public void confirm(OrderConfirmCommand command) {
        // 1. Redis 세션 조회
        OrderSession session = orderSessionRedisRepository.findByOrderNumber(command.orderNumber())
            .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_NOT_FOUND));

        // 2. 사용자 검증
        if (!session.getUserId().equals(command.userId())) {
            throw new BusinessException(UserErrorCode.ORDER_USER_MISMATCH);
        }

        // 3. 금액 검증
        if (session.getPaymentAmount() != command.amount()) {
            throw new BusinessException(UserErrorCode.ORDER_AMOUNT_MISMATCH);
        }

        // 4. 중복 처리 방지 Lock (leaseTime 없이 watchdog 자동 갱신)
        RLock confirmLock = redissonClient.getLock("order:confirm:lock:" + command.orderNumber());
        boolean acquired = false;
        try {
            acquired = confirmLock.tryLock(3, TimeUnit.SECONDS);
            if (!acquired) {
                throw new BusinessException(UserErrorCode.ORDER_ALREADY_PROCESSED);
            }

            // 5. 이미 처리된 주문 확인
            if (ordersRepository.existsByOrderNumber(command.orderNumber())) {
                throw new BusinessException(UserErrorCode.ORDER_ALREADY_PROCESSED);
            }

            // 6. Toss 결제 승인
            TossPaymentResponse tossResponse = tossPaymentClient.confirm(
                command.orderNumber(), command.paymentKey(), command.amount()
            );

            // 7 ~ 10. Toss 승인 이후 DB 처리 — 실패 시 Toss 취소 + Redis 복원
            try {
                // 7. DB 재고 감소
                for (OrderSessionItem item : session.getItems()) {
                    int updated = productDetailsService.decreaseStock(
                        item.getProductDetailId(), item.getQuantity()
                    );
                    if (updated == 0) {
                        log.error("[Order] DB 재고 부족 - productDetailId: {}", item.getProductDetailId());
                        throw new BusinessException(UserErrorCode.OUT_OF_STOCK);
                    }
                }

                // 8. 주문 저장
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

                // 9. 결제 방식에 따른 주문 상태
                if (tossResponse.isVirtualAccount()) {
                    order.waitDeposit();
                } else {
                    order.pay();
                }
                order.update();
                ordersRepository.save(order);

                // 10. 결제 저장
                LocalDateTime approvedAt = tossResponse.getApprovedAt() != null
                    ? tossResponse.getApprovedAt().toLocalDateTime()
                    : LocalDateTime.now();

                Payment payment = Payment.create(
                    order,
                    tossResponse.getPaymentKey(),
                    tossResponse.getCurrency(),
                    tossResponse.getMethod(),
                    tossResponse.getTotalAmount(),
                    tossResponse.getRequestedAt().toLocalDateTime(),
                    approvedAt
                );
                payment.update();
                paymentService.save(payment);

                log.info("[Order] confirm 완료 - orderNumber: {}, method: {}",
                    command.orderNumber(), tossResponse.getMethod());

            } catch (Exception e) {
                log.error("[Order] DB 처리 실패 - Toss 취소 시도 - orderNumber: {}", command.orderNumber(), e);
                try {
                    tossPaymentClient.cancel(command.paymentKey(), "시스템 오류로 인한 자동 취소");
                } catch (Exception cancelEx) {
                    log.error("[Order] Toss 취소 실패 - 수동 취소 필요 - paymentKey: {}", command.paymentKey(), cancelEx);
                }
                session.getItems().forEach(i ->
                    stockCacheService.restoreStock(i.getProductDetailId(), i.getQuantity()));
                stockReserveRedisRepository.delete(command.orderNumber());
                if (e instanceof BusinessException) throw e;
                throw new BusinessException(UserErrorCode.PAYMENT_CONFIRMATION_FAILED);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(UserErrorCode.ORDER_ALREADY_PROCESSED);
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
        // [Phase 1] read tx: 검증 + 필요 데이터 수집 후 커넥션 반환
        CancelPrecheck precheck = readTx().execute(status -> {
            Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                    command.orderNumber(), command.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_NOT_FOUND));

            if (order.getOrderStatus() != OrderStatus.PAID
                && order.getOrderStatus() != OrderStatus.WAITING_DEPOSIT
                && order.getOrderStatus() != OrderStatus.PREPARING_DELIVERY) {
                throw new BusinessException(UserErrorCode.ORDER_CANCEL_NOT_ALLOWED);
            }

            Payment payment = paymentService.findByOrder(order);
            int cancelableAmount = payment.getTotalAmount() - payment.getTotalCancelAmount();
            if (cancelableAmount <= 0) {
                throw new BusinessException(UserErrorCode.ORDER_CANCEL_NOT_ALLOWED);
            }

            return new CancelPrecheck(payment.getPaymentKey(), cancelableAmount);
        });

        // [Phase 2] Toss API 호출 — DB 커넥션 미보유
        tossPaymentClient.cancel(precheck.paymentKey(), command.cancelReason());

        // [Phase 3] write tx: DB 상태 반영
        writeTx().execute(status -> {
            Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                    command.orderNumber(), command.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_NOT_FOUND));
            Payment payment = paymentService.findByOrder(order);

            for (OrderDetails detail : order.getOrderList()) {
                if (detail.getStatus() == OrderDetailStatus.NORMAL) {
                    detail.cancelRequest();
                    detail.cancel();
                }
            }
            order.cancel();
            paymentService.recordCancel(payment, command.cancelReason(), precheck.cancelableAmount());

            for (OrderDetails detail : order.getOrderList()) {
                int updated = productDetailsService.increaseStock(
                    detail.getProductDetail().getId(), detail.getQuantity()
                );
                if (updated != 1) {
                    log.error("[Order] 재고 복원 실패 - productDetailId: {}", detail.getProductDetail().getId());
                    throw new BusinessException(UserErrorCode.STOCK_RESTORE_FAILED);
                }
            }

            List<Long> productDetailIds = order.getOrderList().stream()
                .map(d -> d.getProductDetail().getId())
                .toList();
            eventPublisher.publishEvent(new StockInvalidateEvent(productDetailIds));

            log.info("[Order] 전체 취소 완료 - orderNumber: {}, userId: {}",
                command.orderNumber(), command.userId());
            return null;
        });
    }

    @Override
    public void cancelPartial(OrderPartialCancelCommand command) {
        // [Phase 1] read tx: 검증 + 필요 데이터 수집
        PartialCancelPrecheck precheck = readTx().execute(status -> {
            Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                    command.orderNumber(), command.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_NOT_FOUND));

            if (order.getOrderStatus() != OrderStatus.PAID
                && order.getOrderStatus() != OrderStatus.PREPARING_DELIVERY) {
                throw new BusinessException(UserErrorCode.ORDER_CANCEL_NOT_ALLOWED);
            }

            List<Long> targetIds = command.orderDetailIds();
            List<OrderDetails> targetDetails = order.getOrderList().stream()
                .filter(d -> targetIds.contains(d.getId()))
                .toList();

            if (targetDetails.size() != targetIds.size()) {
                throw new BusinessException(UserErrorCode.ORDER_PARTIAL_CANCEL_INVALID);
            }
            if (targetDetails.stream().anyMatch(d -> d.getStatus() != OrderDetailStatus.NORMAL)) {
                throw new BusinessException(UserErrorCode.ORDER_PARTIAL_CANCEL_INVALID);
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
                throw new BusinessException(UserErrorCode.ORDER_PARTIAL_CANCEL_INVALID);
            }

            List<Long> targetDetailIds = targetDetails.stream().map(OrderDetails::getId).toList();
            return new PartialCancelPrecheck(payment.getPaymentKey(), cancelAmount, targetDetailIds);
        });

        // [Phase 2] Toss API 호출 — DB 커넥션 미보유
        tossPaymentClient.cancel(precheck.paymentKey(), command.cancelReason(), precheck.cancelAmount());

        // [Phase 3] write tx: DB 상태 반영
        writeTx().execute(status -> {
            Orders order = ordersRepository.findOrderDetailByOrderNumberAndUserId(
                    command.orderNumber(), command.userId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_NOT_FOUND));
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

            for (OrderDetails detail : targetDetails) {
                int updated = productDetailsService.increaseStock(
                    detail.getProductDetail().getId(), detail.getQuantity()
                );
                if (updated != 1) {
                    log.error("[Order] 재고 복원 실패 - productDetailId: {}", detail.getProductDetail().getId());
                    throw new BusinessException(UserErrorCode.STOCK_RESTORE_FAILED);
                }
            }

            List<Long> productDetailIds = targetDetails.stream()
                .map(d -> d.getProductDetail().getId())
                .toList();
            eventPublisher.publishEvent(new StockInvalidateEvent(productDetailIds));

            log.info("[Order] 일부 취소 완료 - orderNumber: {}, userId: {}, 취소금액: {}",
                command.orderNumber(), command.userId(), precheck.cancelAmount());
            return null;
        });
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
            .orElseThrow(() -> new BusinessException(UserErrorCode.ORDER_NOT_FOUND));
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
            first != null ? first.getProductDetail().getSize() : null
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

        return new OrderFullDetailResult(
            order.getOrderNumber(),
            order.getOrderStatus(),
            order.getCreatedAt(),
            order.getTotalOriginalAmount(),
            order.getTotalSellingAmount(),
            order.getShippingFee(),
            order.getUsedPoint(),
            order.getPaymentAmount(),
            items
        );
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return date + "-" + random;
    }
}
