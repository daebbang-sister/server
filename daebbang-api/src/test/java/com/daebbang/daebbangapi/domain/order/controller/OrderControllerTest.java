package com.daebbang.daebbangapi.domain.order.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderCancelRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderConfirmRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderItemRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderPartialCancelRequest;
import com.daebbang.daebbangapi.domain.order.dto.request.OrderPrepareRequest;
import com.daebbang.daebbangapi.domain.user.service.CustomUserDetailsService;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import com.daebbang.daebbangcore.domain.order.dto.OrderFullDetailResult;
import com.daebbang.daebbangcore.domain.order.dto.OrderPrepareResponse;
import com.daebbang.daebbangcore.domain.order.dto.OrderStatusCountResult;
import com.daebbang.daebbangcore.domain.order.dto.OrderSummaryResult;
import com.daebbang.daebbangcore.domain.order.entity.OrderDetailStatus;
import com.daebbang.daebbangcore.domain.order.entity.OrderStatus;
import com.daebbang.daebbangcore.domain.order.service.OrderService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    @Test
    @DisplayName("POST /v1/orders/prepare - 주문 준비 성공 (배송비 있음)")
    void prepare_success_withShippingFee() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );
        OrderPrepareResponse response = new OrderPrepareResponse("20260416-ABC1234567", 33_000);
        given(orderService.prepare(any())).willReturn(response);

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (0 이상)"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비 (산간지역 여부에 따라 프론트에서 결정; 5만원 이상 구매 시 0)"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록에 추가 여부 (true면 배송지 목록에 저장)"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 설정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (예: '회사', '집') - isAddToAddressBook=true일 때 사용, 선택")
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
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 2)),
            0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            0, null, false, false, null
        );
        OrderPrepareResponse response = new OrderPrepareResponse("20260416-DEF9876543", 60_000);
        given(orderService.prepare(any())).willReturn(response);

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비 (5만원 이상 구매 시 0)"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            3_000, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );
        OrderPrepareResponse response = new OrderPrepareResponse("20260416-GHI1357924", 30_000);
        given(orderService.prepare(any())).willReturn(response);

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (결제 금액에서 차감)"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(), 0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );

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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
            0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
            -1, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (-1 - 오류)"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
            0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );
        willThrow(new BusinessException(OrderErrorCode.OUT_OF_STOCK))
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
            0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );
        willThrow(new BusinessException(OrderErrorCode.STOCK_LOCK_FAILED))
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
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
    @DisplayName("POST /v1/orders/prepare - 포인트가 결제 금액 초과 시 400 반환")
    void prepare_pointExceedsPayment() throws Exception {
        OrderPrepareRequest request = new OrderPrepareRequest(
            List.of(new OrderItemRequest(1L, 1)),
            99_999, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );
        willThrow(new BusinessException(OrderErrorCode.POINT_EXCEEDS_PAYMENT))
            .given(orderService).prepare(any());

        mockMvc.perform(post("/v1/orders/prepare")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("포인트가 결제 금액을 초과합니다."))
            .andDo(document("order/prepare-point-exceeds",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 준비 - 포인트 초과")
                    .description("사용 포인트가 결제 금액(상품금액 + 배송비)보다 크면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("items").type(JsonFieldType.ARRAY).description("주문 상품 목록"),
                        fieldWithPath("items[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                        fieldWithPath("usedPoint").type(JsonFieldType.NUMBER).description("사용 포인트 (결제 금액 초과)"),
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 연락처"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("기본 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("shippingFee").type(JsonFieldType.NUMBER).description("배송비"),
                        fieldWithPath("orderNote").type(JsonFieldType.STRING).optional().description("배송 요청사항 (100자 이내, 선택)"),
                        fieldWithPath("isAddToAddressBook").type(JsonFieldType.BOOLEAN).description("주소록 추가 여부"),
                        fieldWithPath("isDefaultAddress").type(JsonFieldType.BOOLEAN).description("기본 배송지로 ���정 여부 (isAddToAddressBook=true일 때 적용)"),
                        fieldWithPath("addressAlias").type(JsonFieldType.STRING).optional().description("주소록 별칭 (isAddToAddressBook=true일 때 사용, 선택)")
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
            0, "홍길동", "01012345678", "06123", "서울시 강남구 테헤란로 1", "101동 202호",
            3_000, null, false, false, null
        );

        mockMvc.perform(post("/v1/orders/prepare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/orders/confirm - 결제 확정 성공")
    void confirm_success() throws Exception {
        OrderConfirmRequest request = new OrderConfirmRequest(
            "20260416-ABC1234567", "toss_payment_key_abc", 33_000
        );
        willDoNothing().given(orderService).confirm(any());

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
        willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND))
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
        willThrow(new BusinessException(OrderErrorCode.ORDER_AMOUNT_MISMATCH))
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
        willThrow(new BusinessException(OrderErrorCode.ORDER_USER_MISMATCH))
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
        willThrow(new BusinessException(OrderErrorCode.ORDER_ALREADY_PROCESSED))
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
        willThrow(new BusinessException(OrderErrorCode.PAYMENT_CONFIRMATION_FAILED))
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

    @Test
    @DisplayName("POST /v1/orders/{orderNumber}/cancel - 전체 취소 성공")
    void cancel_success() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest("단순 변심");
        willDoNothing().given(orderService).cancel(any());

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("주문이 취소되었습니다."))
            .andDo(document("order/cancel-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 전체 취소")
                    .description("""
                        결제완료(PAID), 배송준비중(PREPARING_DELIVERY) 상태의 주문을 전체 취소합니다.
                        가상계좌 입금 대기(WAITING_DEPOSIT) 상태도 취소 가능합니다.
                        배송중(IN_DELIVERY) 이후에는 취소가 불가능합니다.
                        취소 성공 시 결제 취소(토스 API 호출), 재고 복원이 자동으로 처리됩니다.
                        """)
                    .requestSchema(Schema.schema("OrderCancelRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유 (1~200자)")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel - cancelReason 빈 문자열이면 400 반환")
    void cancel_blankReason() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest("");

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/cancel-blank-reason",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 전체 취소 - 취소 사유 누락")
                    .description("취소 사유가 빈 문자열이면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유 (빈 문자열 - 오류)")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel - 존재하지 않는 주문이면 404 반환")
    void cancel_orderNotFound() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest("단순 변심");
        willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND))
            .given(orderService).cancel(any());

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel", "20260416-NOTEXIST00")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 주문입니다."))
            .andDo(document("order/cancel-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 전체 취소 - 주문 없음")
                    .description("존재하지 않거나 본인 주문이 아닌 경우 404를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel - 취소 불가 상태(배송중)이면 400 반환")
    void cancel_notAllowed() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest("단순 변심");
        willThrow(new BusinessException(OrderErrorCode.ORDER_CANCEL_NOT_ALLOWED))
            .given(orderService).cancel(any());

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("취소할 수 없는 주문 상태입니다."))
            .andDo(document("order/cancel-not-allowed",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 전체 취소 - 취소 불가 상태")
                    .description("배송중(IN_DELIVERY), 배송완료(DELIVERED) 등 취소 불가 상태의 주문은 400을 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel - 토스 결제 취소 실패 시 502 반환")
    void cancel_paymentCancelFailed() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest("단순 변심");
        willThrow(new BusinessException(OrderErrorCode.PAYMENT_CANCEL_FAILED))
            .given(orderService).cancel(any());

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("결제 취소에 실패했습니다. 관리자에게 문의하세요."))
            .andDo(document("order/cancel-payment-failed",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 전체 취소 - 결제 취소 실패")
                    .description("토스 결제 취소 API 호출에 실패한 경우 502를 반환합니다. DB는 롤백되며 주문 상태는 변경되지 않습니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel - 인증 없이 접근 시 401 반환")
    void cancel_unauthorized() throws Exception {
        OrderCancelRequest request = new OrderCancelRequest("단순 변심");

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel", "20260416-ABC1234567")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderNumber}/cancel/partial - 부분 취소 성공")
    void cancelPartial_success() throws Exception {
        OrderPartialCancelRequest request = new OrderPartialCancelRequest(List.of(10L, 11L), "사이즈 오주문");
        willDoNothing().given(orderService).cancelPartial(any());

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel/partial", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("주문이 취소되었습니다."))
            .andDo(document("order/cancel-partial-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 부분 취소")
                    .description("""
                        주문 내 특정 상품(orderDetailId)만 선택적으로 취소합니다.
                        선택한 항목의 금액만큼 부분 환불이 진행됩니다.
                        모든 항목이 취소되면 주문 전체가 CANCELLED 상태로 변경됩니다.
                        이미 취소된 항목을 포함하거나, 다른 주문의 항목 ID를 전달하면 400을 반환합니다.
                        """)
                    .requestSchema(Schema.schema("OrderPartialCancelRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("orderDetailIds").type(JsonFieldType.ARRAY).description("취소할 주문 항목 ID 목록 (1개 이상)"),
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유 (1~200자)")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel/partial - orderDetailIds 빈 배열이면 400 반환")
    void cancelPartial_emptyDetailIds() throws Exception {
        OrderPartialCancelRequest request = new OrderPartialCancelRequest(List.of(), "사이즈 오주문");

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel/partial", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("order/cancel-partial-empty-ids",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 부분 취소 - 항목 미선택")
                    .description("orderDetailIds가 비어있으면 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("orderDetailIds").type(JsonFieldType.ARRAY).description("취소할 항목 ID 목록 (비어있음 - 오류)"),
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유")
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
    @DisplayName("POST /v1/orders/{orderNumber}/cancel/partial - 유효하지 않은 항목(이미 취소됨)이면 400 반환")
    void cancelPartial_invalidItem() throws Exception {
        OrderPartialCancelRequest request = new OrderPartialCancelRequest(List.of(10L), "사이즈 오주문");
        willThrow(new BusinessException(OrderErrorCode.ORDER_PARTIAL_CANCEL_INVALID))
            .given(orderService).cancelPartial(any());

        mockMvc.perform(post("/v1/orders/{orderNumber}/cancel/partial", "20260416-ABC1234567")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("유효하지 않은 취소 항목입니다."))
            .andDo(document("order/cancel-partial-invalid",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 부분 취소 - 유효하지 않은 항목")
                    .description("이미 취소된 항목이거나 해당 주문에 속하지 않는 항목 ID가 포함된 경우 400을 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .requestFields(
                        fieldWithPath("orderDetailIds").type(JsonFieldType.ARRAY).description("취소할 항목 ID 목록 (이미 취소된 항목 포함)"),
                        fieldWithPath("cancelReason").type(JsonFieldType.STRING).description("취소 사유")
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
    @DisplayName("GET /v1/orders - 주문 목록 조회 성공 (기본 3개월)")
    void getOrderList_success() throws Exception {
        OrderSummaryResult summary = new OrderSummaryResult(
            1L, "20260416-ABC1234567", OrderStatus.PAID,
            LocalDateTime.of(2026, 4, 16, 10, 0, 0),
            33_000, 2, "슬림핏 청바지", "https://cdn.daebbang.com/img/slim-jeans.jpg",
            "골드", "M", 30_000, 1
        );
        Page<OrderSummaryResult> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1);
        given(orderService.getOrderList(any(), any(), any(), any())).willReturn(page);

        mockMvc.perform(get("/v1/orders")
                .with(authentication(authToken()))
                .param("page", "0")
                .param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("주문 목록 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.content[0].orderNumber").value("20260416-ABC1234567"))
            .andExpect(jsonPath("$.data.content[0].orderStatus").value("PAID"))
            .andExpect(jsonPath("$.data.content[0].paymentAmount").value(33_000))
            .andExpect(jsonPath("$.data.content[0].totalQuantity").value(2))
            .andExpect(jsonPath("$.data.content[0].representativeProductName").value("슬림핏 청바지"))
            .andDo(document("order/list-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 목록 조회")
                    .description("""
                        로그인한 사용자의 주문 목록을 페이징하여 반환합니다.
                        기본 조회 범위는 최근 3개월이며 최대 1년까지 조회 가능합니다.
                        startDate를 1년 이전으로 지정하면 자동으로 1년 전으로 보정됩니다.
                        날짜 형식: yyyy-MM-dd
                        """)
                    .responseSchema(Schema.schema("OrderListResponse"))
                    .queryParameters(
                        parameterWithName("startDate").optional().description("조회 시작일 (yyyy-MM-dd, 기본값: 오늘 - 3개월, 최대 1년 전)"),
                        parameterWithName("endDate").optional().description("조회 종료일 (yyyy-MM-dd, 기본값: 오늘)"),
                        parameterWithName("page").optional().description("페이지 번호 (0부터 시작, 기본값: 0)"),
                        parameterWithName("size").optional().description("페이지 크기 (기본값: 10)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("주문 목록"),
                        fieldWithPath("data.content[].orderNumber").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("data.content[].orderStatus").type(JsonFieldType.STRING).description("주문 상태 (PAID, PREPARING_DELIVERY, IN_DELIVERY, DELIVERED, COMPLETED, CANCELLED 등)"),
                        fieldWithPath("data.content[].orderedAt").type(JsonFieldType.STRING).description("주문 일시"),
                        fieldWithPath("data.content[].paymentAmount").type(JsonFieldType.NUMBER).description("결제 금액"),
                        fieldWithPath("data.content[].totalQuantity").type(JsonFieldType.NUMBER).description("총 상품 수량"),
                        fieldWithPath("data.content[].representativeProductName").type(JsonFieldType.STRING).description("대표 상품명 (첫 번째 상품)"),
                        fieldWithPath("data.content[].representativeImageUrl").type(JsonFieldType.STRING).description("대표 상품 이미지 URL"),
                        fieldWithPath("data.content[].representativeColor").type(JsonFieldType.STRING).description("대표 상품 색상"),
                        fieldWithPath("data.content[].representativeSize").type(JsonFieldType.STRING).description("대표 상품 사이즈"),
                        fieldWithPath("data.content[].representativeUnitPrice").type(JsonFieldType.NUMBER).description("대표 상품 단가 (할인 적용 후)"),
                        fieldWithPath("data.content[].representativeQuantity").type(JsonFieldType.NUMBER).description("대표 상품 주문 수량"),
                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("페이지 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER).description("오프셋"),
                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이징 적용 여부"),
                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("페이징 미적용 여부"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 주문 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 항목 수"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("결과 없음 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/orders - 날짜 범위 지정 조회 성공")
    void getOrderList_withDateRange() throws Exception {
        given(orderService.getOrderList(any(), any(), any(), any()))
            .willReturn(Page.empty());

        mockMvc.perform(get("/v1/orders")
                .with(authentication(authToken()))
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-03-31"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andDo(document("order/list-with-date-range",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 목록 조회 - 날짜 범위 지정")
                    .description("startDate와 endDate를 지정하여 특정 기간의 주문을 조회합니다.")
                    .responseSchema(Schema.schema("OrderListResponse"))
                    .queryParameters(
                        parameterWithName("startDate").optional().description("조회 시작일 (2026-01-01)"),
                        parameterWithName("endDate").optional().description("조회 종료일 (2026-03-31)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("주문 목록 (결과 없음)"),
                        fieldWithPath("data.pageable").type(JsonFieldType.STRING).description("페이지 정보"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 주문 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 항목 수"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("결과 없음 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/orders - 인증 없이 접근 시 401 반환")
    void getOrderList_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/orders"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /v1/orders/{orderNumber} - 주문 상세 조회 성공")
    void getOrderDetail_success() throws Exception {
        OrderFullDetailResult.OrderDetailItem item = new OrderFullDetailResult.OrderDetailItem(
            10L, 101L, "슬림핏 청바지",
            "https://cdn.daebbang.com/img/slim-jeans.jpg",
            "골드", "#FFD700", "M",
            2, 15_000, 10, 13_500, OrderDetailStatus.NORMAL
        );
        OrderFullDetailResult result = new OrderFullDetailResult(
            "20260416-ABC1234567", OrderStatus.PAID,
            LocalDateTime.of(2026, 4, 16, 10, 0, 0),
            30_000, 27_000, 3_000, 0, 30_000,
            List.of(item)
        );
        given(orderService.getOrderDetail(any(), any())).willReturn(result);

        mockMvc.perform(get("/v1/orders/{orderNumber}", "20260416-ABC1234567")
                .with(authentication(authToken())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("주문 상세 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.orderNumber").value("20260416-ABC1234567"))
            .andExpect(jsonPath("$.data.orderStatus").value("PAID"))
            .andExpect(jsonPath("$.data.totalOriginalAmount").value(30_000))
            .andExpect(jsonPath("$.data.totalSellingAmount").value(27_000))
            .andExpect(jsonPath("$.data.shippingFee").value(3_000))
            .andExpect(jsonPath("$.data.usedPoint").value(0))
            .andExpect(jsonPath("$.data.paymentAmount").value(30_000))
            .andExpect(jsonPath("$.data.details[0].orderDetailId").value(10))
            .andExpect(jsonPath("$.data.details[0].productDetailId").value(101))
            .andExpect(jsonPath("$.data.details[0].productName").value("슬림핏 청바지"))
            .andExpect(jsonPath("$.data.details[0].status").value("NORMAL"))
            .andDo(document("order/detail-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 상세 조회")
                    .description("""
                        주문 번호로 해당 주문의 상세 정보를 조회합니다.
                        본인 주문만 조회 가능하며 타인의 주문 번호를 전달하면 404를 반환합니다.
                        orderDetailId를 통해 부분 취소 API에서 항목을 지정할 수 있습니다.
                        """)
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
                    .responseSchema(Schema.schema("OrderDetailResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.orderNumber").type(JsonFieldType.STRING).description("주문 번호"),
                        fieldWithPath("data.orderStatus").type(JsonFieldType.STRING).description("주문 상태"),
                        fieldWithPath("data.orderedAt").type(JsonFieldType.STRING).description("주문 일시"),
                        fieldWithPath("data.totalOriginalAmount").type(JsonFieldType.NUMBER).description("상품 정가 합계"),
                        fieldWithPath("data.totalSellingAmount").type(JsonFieldType.NUMBER).description("할인 적용 판매가 합계"),
                        fieldWithPath("data.shippingFee").type(JsonFieldType.NUMBER).description("배송비 (5만원 이상 무료)"),
                        fieldWithPath("data.usedPoint").type(JsonFieldType.NUMBER).description("사용한 포인트"),
                        fieldWithPath("data.paymentAmount").type(JsonFieldType.NUMBER).description("최종 결제 금액"),
                        fieldWithPath("data.details[]").type(JsonFieldType.ARRAY).description("주문 항목 목록"),
                        fieldWithPath("data.details[].orderDetailId").type(JsonFieldType.NUMBER).description("주문 항목 ID (부분 취소 시 사용)"),
                        fieldWithPath("data.details[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID (색상/사이즈 조합)"),
                        fieldWithPath("data.details[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.details[].imageUrl").type(JsonFieldType.STRING).description("상품 이미지 URL"),
                        fieldWithPath("data.details[].color").type(JsonFieldType.STRING).description("색상명"),
                        fieldWithPath("data.details[].colorCode").type(JsonFieldType.STRING).description("색상 코드 (HEX)"),
                        fieldWithPath("data.details[].size").type(JsonFieldType.STRING).description("사이즈"),
                        fieldWithPath("data.details[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                        fieldWithPath("data.details[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.details[].discountRate").type(JsonFieldType.NUMBER).description("할인율 (%)"),
                        fieldWithPath("data.details[].discountPrice").type(JsonFieldType.NUMBER).description("할인 적용가"),
                        fieldWithPath("data.details[].status").type(JsonFieldType.STRING).description("항목 상태 (NORMAL, CANCEL_REQUESTED, CANCELLED, REFUND_REQUESTED, RETURNED)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/orders/{orderNumber} - 존재하지 않는 주문이면 404 반환")
    void getOrderDetail_notFound() throws Exception {
        willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND))
            .given(orderService).getOrderDetail(any(), any());

        mockMvc.perform(get("/v1/orders/{orderNumber}", "20260416-NOTEXIST00")
                .with(authentication(authToken())))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("존재하지 않는 주문입니다."))
            .andDo(document("order/detail-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("주문 상세 조회 - 주문 없음")
                    .description("존재하지 않거나 본인 주문이 아닌 경우 404를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(parameterWithName("orderNumber").description("주문 번호"))
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
    @DisplayName("GET /v1/orders/{orderNumber} - 인증 없이 접근 시 401 반환")
    void getOrderDetail_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/orders/{orderNumber}", "20260416-ABC1234567"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /v1/orders/status-count - 배송 현황 건수 조회 성공")
    void getOrderStatusCount_success() throws Exception {
        OrderStatusCountResult result = new OrderStatusCountResult(2L, 1L, 3L, 1L);
        given(orderService.getOrderStatusCount(any(), any(), any())).willReturn(result);

        mockMvc.perform(get("/v1/orders/status-count")
                .with(authentication(authToken())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("배송 현황 건수 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.paid").value(2))
            .andExpect(jsonPath("$.data.preparingDelivery").value(1))
            .andExpect(jsonPath("$.data.inDelivery").value(3))
            .andExpect(jsonPath("$.data.delivered").value(1))
            .andDo(document("order/status-count-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("배송 현황 건수 조회")
                    .description("""
                        마이페이지 상단에 표시할 배송 현황 건수를 반환합니다.
                        결제완료 / 배송준비중 / 배송중 / 배송완료 4가지 상태별 건수를 제공합니다.
                        날짜 범위를 지정하지 않으면 기본 3개월 기준으로 집계하며 최대 1년까지 조회 가능합니다.
                        """)
                    .responseSchema(Schema.schema("OrderStatusCountResponse"))
                    .queryParameters(
                        parameterWithName("startDate").optional().description("조회 시작일 (yyyy-MM-dd, 기본값: 오늘 - 3개월)"),
                        parameterWithName("endDate").optional().description("조회 종료일 (yyyy-MM-dd, 기본값: 오늘)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.paid").type(JsonFieldType.NUMBER).description("결제완료(PAID) 건수"),
                        fieldWithPath("data.preparingDelivery").type(JsonFieldType.NUMBER).description("배송준비중(PREPARING_DELIVERY) 건수"),
                        fieldWithPath("data.inDelivery").type(JsonFieldType.NUMBER).description("배송중(IN_DELIVERY) 건수"),
                        fieldWithPath("data.delivered").type(JsonFieldType.NUMBER).description("배송완료(DELIVERED) 건수")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/orders/status-count - 날짜 범위 지정 조회 성공")
    void getOrderStatusCount_withDateRange() throws Exception {
        OrderStatusCountResult result = new OrderStatusCountResult(0L, 0L, 0L, 0L);
        given(orderService.getOrderStatusCount(any(), any(), any())).willReturn(result);

        mockMvc.perform(get("/v1/orders/status-count")
                .with(authentication(authToken()))
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-01-31"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paid").value(0))
            .andExpect(jsonPath("$.data.preparingDelivery").value(0))
            .andExpect(jsonPath("$.data.inDelivery").value(0))
            .andExpect(jsonPath("$.data.delivered").value(0))
            .andDo(document("order/status-count-with-date-range",
                resource(ResourceSnippetParameters.builder()
                    .tag("Order")
                    .summary("배송 현황 건수 조회 - 날짜 범위 지정")
                    .description("특정 기간의 배송 현황 건수를 조회합니다. 해당 기간에 주문이 없으면 모두 0으로 반환합니다.")
                    .responseSchema(Schema.schema("OrderStatusCountResponse"))
                    .queryParameters(
                        parameterWithName("startDate").optional().description("조회 시작일 (2026-01-01)"),
                        parameterWithName("endDate").optional().description("조회 종료일 (2026-01-31)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.paid").type(JsonFieldType.NUMBER).description("결제완료 건수"),
                        fieldWithPath("data.preparingDelivery").type(JsonFieldType.NUMBER).description("배송준비중 건수"),
                        fieldWithPath("data.inDelivery").type(JsonFieldType.NUMBER).description("배송중 건수"),
                        fieldWithPath("data.delivered").type(JsonFieldType.NUMBER).description("배송완료 건수")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/orders/status-count - 인증 없이 접근 시 401 반환")
    void getOrderStatusCount_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/orders/status-count"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
