package com.daebbang.daebbangcore.domain.order.service.impl;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.order.command.OrderConfirmCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderItemCommand;
import com.daebbang.daebbangcore.domain.order.command.OrderPrepareCommand;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetails;
import com.daebbang.daebbangcore.domain.order.entity.Orders;
import com.daebbang.daebbangcore.domain.order.entity.Payment;
import com.daebbang.daebbangcore.domain.order.repository.OrdersRepository;
import com.daebbang.daebbangcore.domain.order.repository.PaymentRepository;
import com.daebbang.daebbangcore.domain.order.service.OrderService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PaymentRepository paymentRepository;
    private final StockCacheService stockCacheService;
    private final OrderSessionRedisRepository orderSessionRedisRepository;
    private final TossPaymentClient tossPaymentClient;
    private final RedissonClient redissonClient;
    private final OrderStockReserveRedisRepository stockReserveRedisRepository;

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
                    // merge로 동일 productDetailId 중복 요청 처리
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
                paymentRepository.save(payment);

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

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return date + "-" + random; // 8 + 1 + 10 = 19자
    }
}
