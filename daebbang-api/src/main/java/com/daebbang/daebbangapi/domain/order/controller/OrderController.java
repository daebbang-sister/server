package com.daebbang.daebbangapi.domain.order.controller;

import com.daebbang.daebbangapi.domain.order.dto.request.OrderCancelRequest;
import com.daebbang.daebbangcore.domain.order.entity.PaymentMethod;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderConfirmRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderPartialCancelRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderPrepareRequest;
import com.daebbang.daebbangapi.domain.order.dto.response.OrderDetailResponse;
import com.daebbang.daebbangapi.domain.order.dto.response.OrderListItemResponse;
import com.daebbang.daebbangapi.domain.order.dto.response.OrderStatusCountResponse;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.service.OrderService;
import jakarta.validation.Valid;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/prepare")
    public ResponseEntity<@NonNull CommonResponse<OrderPrepareResponse>> prepareOrder(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody OrderPrepareRequest request
    ) {
        OrderPrepareResponse response = orderService.prepare(request.toCommand(userId));
        UserSuccessCode code = request.paymentMethod() == PaymentMethod.BANK_TRANSFER
            ? UserSuccessCode.ORDER_PREPARED_BANK_TRANSFER
            : UserSuccessCode.ORDER_PREPARED;
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(CommonResponse.success(code, response));
    }

    @PostMapping("/confirm")
    public ResponseEntity<@NonNull CommonResponse<Void>> confirmOrder(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody OrderConfirmRequest request
    ) {
        orderService.confirm(request.toCommand(userId));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.ORDER_CONFIRMED));
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<@NonNull CommonResponse<Void>> cancelOrder(
        @AuthenticationPrincipal Long userId,
        @PathVariable String orderNumber,
        @Valid @RequestBody OrderCancelRequest request
    ) {
        orderService.cancel(request.toCommand(userId, orderNumber));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.ORDER_CANCELLED));
    }

    @PostMapping("/{orderNumber}/complete")
    public ResponseEntity<@NonNull CommonResponse<Void>> completeOrder(
        @AuthenticationPrincipal Long userId,
        @PathVariable String orderNumber
    ) {
        orderService.complete(userId, orderNumber);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.ORDER_COMPLETED));
    }

    @PostMapping("/{orderNumber}/cancel/partial")
    public ResponseEntity<@NonNull CommonResponse<Void>> cancelOrderPartial(
        @AuthenticationPrincipal Long userId,
        @PathVariable String orderNumber,
        @Valid @RequestBody OrderPartialCancelRequest request
    ) {
        orderService.cancelPartial(request.toCommand(userId, orderNumber));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.ORDER_CANCELLED));
    }

    @GetMapping
    public ResponseEntity<@NonNull CommonResponse<Page<@NonNull OrderListItemResponse>>> getOrderList(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        LocalDateTime end = resolveEndDate(endDate);
        LocalDateTime start = resolveStartDate(startDate, end);

        Page<@NonNull OrderListItemResponse> response = orderService.getOrderList(userId, start, end, pageable)
            .map(OrderListItemResponse::from);
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.ORDER_LIST_RETRIEVED, response));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<@NonNull CommonResponse<OrderDetailResponse>> getOrderDetail(
        @AuthenticationPrincipal Long userId,
        @PathVariable String orderNumber
    ) {
        OrderDetailResponse response = OrderDetailResponse.from(orderService.getOrderDetail(userId, orderNumber));
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.ORDER_DETAIL_RETRIEVED, response));
    }

    @GetMapping("/status-count")
    public ResponseEntity<@NonNull CommonResponse<OrderStatusCountResponse>> getOrderStatusCount(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDateTime end = resolveEndDate(endDate);
        LocalDateTime start = resolveStartDate(startDate, end);

        OrderStatusCountResponse response = OrderStatusCountResponse.from(
            orderService.getOrderStatusCount(userId, start, end)
        );
        return ResponseEntity.ok(CommonResponse.success(UserSuccessCode.ORDER_STATUS_COUNT_RETRIEVED, response));
    }

    private LocalDateTime resolveStartDate(LocalDate startDate, LocalDateTime end) {
        LocalDateTime oneYearAgo = end.minusYears(1);
        if (startDate == null) {
            return end.minusMonths(3);
        }
        LocalDateTime start = startDate.atStartOfDay();
        if (start.isAfter(end)) {
            throw new BusinessException(OrderErrorCode.ORDER_DATE_RANGE_INVALID);
        }
        return start.isBefore(oneYearAgo) ? oneYearAgo : start;
    }

    private LocalDateTime resolveEndDate(LocalDate endDate) {
        return endDate != null
            ? endDate.atTime(LocalTime.MAX)
            : LocalDateTime.now();
    }
}
