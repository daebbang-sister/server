package com.daebbang.daebbangapi.domain.order.controller;

import com.daebbang.daebbangapi.domain.order.dto.request.OrderConfirmRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderPrepareRequest;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 준비
     * 재고 검증 후 Redis에 임시 주문 저장, orderNumber 반환
     */
    @PostMapping("/prepare")
    public ResponseEntity<@NonNull CommonResponse<OrderPrepareResponse>> prepareOrder(
        @AuthenticationPrincipal Long userId,
        @Valid @RequestBody OrderPrepareRequest request
    ) {
        OrderPrepareResponse response = orderService.prepare(request.toCommand(userId));
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(CommonResponse.success(UserSuccessCode.ORDER_PREPARED, response));
    }

    /**
     * 결제 확정
     * Toss 승인 API 호출 후 DB 저장
     */
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
}
