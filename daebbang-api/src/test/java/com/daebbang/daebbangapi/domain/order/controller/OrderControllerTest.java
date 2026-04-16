package com.daebbang.daebbangapi.domain.order.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderConfirmRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderItemRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderPrepareRequest;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.service.OrderService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    private static final Long USER_ID = 1L;

    private UsernamePasswordAuthenticationToken authToken() {
        return new UsernamePasswordAuthenticationToken(
            USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // ======================== POST /v1/orders/prepare ========================

    @Test
    @DisplayName("POST /v1/orders/prepare - 주문 준비 성공 (배송비 있음)")
    void prepare_success_withShippingFee() throws Exception {
        // given - 30,000원짜리 1개 → 배송비 3,000원 발생
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            0
        );
        OrderPrepareResponse response = new OrderPrepareResponse("20260416-ABC1234567", 33_000);
        given(orderService.prepare(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("주문이 생성되었습니다."))
            .andExpect(jsonPath("$.data.orderNumber").value("20260416-ABC1234567"))
            .andExpect(jsonPath("$.data.paymentAmount").value(33_000))
            .andDo(document("order/prepare-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비")
                    .description("""
                        결제창 진입 전 재고를 검증하고 Redis에 주문 세션을 임시 저장합니다.
                        반환된 orderNumber와 paymentAmount를 토스 결제창에 전달하세요.
                        주문 세션은 10분 후 자동 만료됩니다.
                        """)
                    .requestSchema(Schema.schema("OrderPrepareRequest"))
                    .responseSchema(Schema.schema("OrderPrepareResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록 (1개 이상)"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID (색상/사이즈 조합)"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("주문 수량 (최소 1)"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (0 이상)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING).description("주문 번호 (토스 결제창의 orderId로 전달)"),
                        fieldWithPath("data.paymentAmount").type(JsonFieldType.NUMBER).description("최종 결제 금액 (배송비 + 상품금액 - 포인트)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - 주문 준비 성공 (5만원 이상 무료 배송)")
    void prepare_success_freeShipping() throws Exception {
        // given - 50,000원 이상 → 배송비 0원
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 2)),
            0
        );
        OrderPrepareResponse response = new OrderPrepareResponse("20260416-DEF9876543", 60_000);
        given(orderService.prepare(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentAmount").value(60_000))
            .andDo(document("order/prepare-success-free-shipping",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 무료 배송")
                    .description("총 판매가가 50,000원 이상이면 배송비가 무료입니다.")
                    .requestSchema(Schema.schema("OrderPrepareRequest"))
                    .responseSchema(Schema.schema("OrderPrepareResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("주문 수량"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("data.paymentAmount").type(JsonFieldType.NUMBER).description("최종 결제 금액 (배송비 0원)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - 주문 준비 성공 (포인트 사용)")
    void prepare_success_withPoint() throws Exception {
        // given - 포인트 3,000 사용
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            3_000
        );
        OrderPrepareResponse response = new OrderPrepareResponse("20260416-GHI1357924", 30_000);
        given(orderService.prepare(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentAmount").value(30_000))
            .andDo(document("order/prepare-success-with-point",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 포인트 사용")
                    .description("포인트를 사용하면 최종 결제 금액에서 차감됩니다.")
                    .requestSchema(Schema.schema("OrderPrepareRequest"))
                    .responseSchema(Schema.schema("OrderPrepareResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("주문 수량"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (결제 금액에서 차감)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("data.paymentAmount").type(JsonFieldType.NUMBER).description("최종 결제 금액 (포인트 차감 후)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - items 빈 배열이면 400 반환")
    void prepare_emptyItems() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(List.of(), 0);

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/prepare-empty-items",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 상품 목록 없음")
                    .description("items가 비어있으면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록 (비어있음 - 오류)"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - quantity 0 이하면 400 반환")
    void prepare_invalidQuantity() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 0)),
            0
        );

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/prepare-invalid-quantity",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 수량 오류")
                    .description("수량이 1 미만이면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("수량 (0 - 오류)"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - usedPoint 음수이면 400 반환")
    void prepare_negativeUsedPoint() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            -1
        );

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/prepare-negative-point",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 포인트 오류")
                    .description("사용 포인트가 음수이면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (-1 - 오류)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - 재고 부족 시 409 반환")
    void prepare_outOfStock() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 999)),
            0
        );
        willThrow(new BusinessException(UserErrorCode.OUT_OF_STOCK))
            .given(orderService).prepare(any());

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("재고가 부족합니다."))
            .andDo(document("order/prepare-out-of-stock",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 재고 부족")
                    .description("요청 수량이 남은 재고보다 많으면 409 Conflict를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("수량 (재고 초과)"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - 재고 락 획득 실패 시 503 반환")
    void prepare_stockLockFailed() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            0
        );
        willThrow(new BusinessException(UserErrorCode.STOCK_LOCK_FAILED))
            .given(orderService).prepare(any());

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.message").value("재고 처리 중입니다. 잠시 후 다시 시도해주세요."))
            .andDo(document("order/prepare-lock-failed",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 재고 락 실패")
                    .description("동시에 같은 상품에 주문이 몰려 락 획득에 실패한 경우 503을 반환합니다. 잠시 후 재시도하세요.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/prepare - 인증 없이 접근 시 401 반환")
    void prepare_unauthorized() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            0
        );

        mockMvc.perform(post("/v1/orders/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== POST /v1/orders/confirm ========================

    @Test
    @DisplayName("POST /v1/orders/confirm - 결제 확정 성공")
    void confirm_success() throws Exception {
        // given
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 33_000
        );
        willDoNothing().given(orderService).confirm(any());

        // when & then
        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("결제가 완료되었습니다."))
            .andDo(document("order/confirm-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정")
                    .description("""
                        토스 결제창 완료 후 서버에서 토스 결제 승인 API를 호출하고 DB에 저장합니다.
                        orderId에는 prepare 응답의 orderNumber를 전달하세요.
                        paymentKey와 amount는 토스 결제창에서 반환된 값을 그대로 전달하세요.
                        """)
                    .requestSchema(Schema.schema("OrderConfirmRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호 (prepare 응답의 orderNumber)"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키 (토스 결제창 완료 후 반환)"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액 (토스 결제창 완료 후 반환)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - orderId 빈 문자열이면 400 반환")
    void confirm_blankOrderId() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest("", "toss_payment_key_abc", 33_000);

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/confirm-blank-order-id",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - orderId 누락")
                    .description("orderId가 빈 문자열이면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호 (빈 문자열 - 오류)"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - paymentKey 빈 문자열이면 400 반환")
    void confirm_blankPaymentKey() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest("20260416-ABC1234567", "", 33_000);

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/confirm-blank-payment-key",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - paymentKey 누락")
                    .description("paymentKey가 빈 문자열이면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키 (빈 문자열 - 오류)"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - amount 0 이하이면 400 반환")
    void confirm_invalidAmount() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest("20260416-ABC1234567", "toss_payment_key_abc", 0);

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/confirm-invalid-amount",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - 금액 오류")
                    .description("amount가 1 미만이면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액 (0 - 오류)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 주문 세션 없음(만료) 시 404 반환")
    void confirm_orderNotFound() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-EXPIRED0000", "toss_payment_key_abc", 33_000
        );
        willThrow(new BusinessException(UserErrorCode.ORDER_NOT_FOUND))
            .given(orderService).confirm(any());

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("존재하지 않는 주문입니다."))
            .andDo(document("order/confirm-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - 주문 세션 없음")
                    .description("Redis 주문 세션이 없거나(10분 만료) 잘못된 orderNumber이면 404를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호 (세션 만료 또는 존재하지 않음)"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 결제 금액 불일치 시 400 반환")
    void confirm_amountMismatch() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 99_999
        );
        willThrow(new BusinessException(UserErrorCode.ORDER_AMOUNT_MISMATCH))
            .given(orderService).confirm(any());

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("결제 금액이 주문 금액과 일치하지 않습니다."))
            .andDo(document("order/confirm-amount-mismatch",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - 금액 불일치")
                    .description("클라이언트가 전달한 amount가 서버에 저장된 주문 금액과 다를 경우 400을 반환합니다. 금액 변조 방지 검증입니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액 (서버 금액과 불일치)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 주문자 불일치 시 403 반환")
    void confirm_userMismatch() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 33_000
        );
        willThrow(new BusinessException(UserErrorCode.ORDER_USER_MISMATCH))
            .given(orderService).confirm(any());

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.message").value("주문자 정보가 일치하지 않습니다."))
            .andDo(document("order/confirm-user-mismatch",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - 주문자 불일치")
                    .description("주문을 생성한 사용자와 결제 확정을 요청한 사용자가 다를 경우 403을 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 이미 처리된 주문이면 409 반환")
    void confirm_alreadyProcessed() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 33_000
        );
        willThrow(new BusinessException(UserErrorCode.ORDER_ALREADY_PROCESSED))
            .given(orderService).confirm(any());

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("이미 처리된 주문입니다."))
            .andDo(document("order/confirm-already-processed",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - 중복 요청")
                    .description("이미 결제 확정이 완료된 주문에 중복 요청하거나 동시에 같은 주문을 처리하려 할 경우 409를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호 (이미 처리됨)"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 토스 결제 승인 실패 시 502 반환")
    void confirm_paymentConfirmationFailed() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 33_000
        );
        willThrow(new BusinessException(UserErrorCode.PAYMENT_CONFIRMATION_FAILED))
            .given(orderService).confirm(any());

        mockMvc.perform(post("/v1/orders/confirm")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(502))
            .andExpect(jsonPath("$.message").value("결제 승인에 실패했습니다. 관리자에게 문의하세요."))
            .andDo(document("order/confirm-payment-failed",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("결제 확정 - 토스 승인 실패")
                    .description("토스 페이먼츠 결제 승인 API 호출에 실패한 경우 502를 반환합니다. 관리자에게 문의하세요.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("orderId").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("paymentKey").type(JsonFieldType.STRING).description("토스 결제키"),
                        fieldWithPath("amount").type(JsonFieldType.NUMBER).description("결제 금액")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 인증 없이 접근 시 401 반환")
    void confirm_unauthorized() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 33_000
        );

        mockMvc.perform(post("/v1/orders/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
