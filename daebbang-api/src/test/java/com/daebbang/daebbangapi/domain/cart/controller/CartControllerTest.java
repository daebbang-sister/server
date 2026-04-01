package com.daebbang.daebbangapi.domain.cart.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.cart.dto.request.CartSaveRequest;
import com.daebbang.daebbangapi.domain.cart.dto.request.CartUpdate;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.cart.entity.Carts;
import com.daebbang.daebbangcore.domain.cart.service.CartService;
import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import com.daebbang.daebbangcore.domain.product.entity.ProductDetails;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.time.LocalDate;
import java.util.ArrayList;
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

@WebMvcTest(controllers = CartController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

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

    /**
     * DiscountType.NONE - 할인 없는 장바구니 항목 mock
     * DiscountCalculator.calculate() → DiscountResult.noDiscount(originalPrice)
     * sellingPrice = originalPrice, discountRate = null
     */
    private Carts mockCartNoDiscount(Long cartId, int quantity) {
        Carts cart = mock(Carts.class);
        ProductDetails productDetail = mock(ProductDetails.class);
        Products product = mock(Products.class);

        given(cart.getId()).willReturn(cartId);
        given(cart.getQuantity()).willReturn(quantity);
        given(cart.getProductDetail()).willReturn(productDetail);
        given(productDetail.getProduct()).willReturn(product);
        given(productDetail.getColor()).willReturn("블랙");
        given(productDetail.getSize()).willReturn("M");
        given(product.getProductName()).willReturn("테스트 상품");
        given(product.getOriginalPrice()).willReturn(30000);
        given(product.getDiscountType()).willReturn(DiscountType.NONE);
        given(product.getDiscountRate()).willReturn(null);
        given(product.getDiscountStartDate()).willReturn(null);
        given(product.getDiscountEndDate()).willReturn(null);
        given(product.getMainImageUrl()).willReturn("https://example.com/image.jpg");

        return cart;
    }

    /**
     * DiscountType.ALWAYS - 상시 할인 장바구니 항목 mock
     * DiscountCalculator.calculate() → DiscountResult.discounted(sellingPrice, discountRate)
     * sellingPrice = floor(30000 * (100 - 10) / 100.0) = 27000, discountRate = 10
     */
    private Carts mockCartAlwaysDiscount(Long cartId, int quantity) {
        Carts cart = mock(Carts.class);
        ProductDetails productDetail = mock(ProductDetails.class);
        Products product = mock(Products.class);

        given(cart.getId()).willReturn(cartId);
        given(cart.getQuantity()).willReturn(quantity);
        given(cart.getProductDetail()).willReturn(productDetail);
        given(productDetail.getProduct()).willReturn(product);
        given(productDetail.getColor()).willReturn("화이트");
        given(productDetail.getSize()).willReturn("L");
        given(product.getProductName()).willReturn("할인 상품");
        given(product.getOriginalPrice()).willReturn(30000);
        given(product.getDiscountType()).willReturn(DiscountType.ALWAYS);
        given(product.getDiscountRate()).willReturn(10);
        given(product.getDiscountStartDate()).willReturn(null);
        given(product.getDiscountEndDate()).willReturn(null);
        given(product.getMainImageUrl()).willReturn("https://example.com/sale-image.jpg");

        return cart;
    }

    private List<Carts> mockCartsNoDiscount(int count, long startId) {
        List<Carts> carts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            carts.add(mockCartNoDiscount(startId - i, 1));
        }
        return carts;
    }

    // ======================== GET /v1/carts ========================

    @Test
    @DisplayName("GET /v1/carts - 첫 페이지 조회 성공 (cursor 없음, hasNext=true)")
    void getCarts_firstPage_success() throws Exception {
        // given - size=8이므로 service는 9개 요청, 9개 반환 → hasNext=true, nextCursor=마지막 id
        List<Carts> mockResult = mockCartsNoDiscount(9, 10L); // id: 10,9,8,7,6,5,4,3,2
        given(cartService.getCarts(anyLong(), any(), anyInt())).willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/v1/carts")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.carts.length()").value(8))
            .andExpect(jsonPath("$.data.hasNext").value(true))
            .andExpect(jsonPath("$.data.nextCursor").value(3))
            .andExpect(jsonPath("$.data.carts[0].cartId").value(10))
            .andExpect(jsonPath("$.data.carts[0].quantity").value(1))
            .andExpect(jsonPath("$.data.carts[0].productName").value("테스트 상품"))
            .andExpect(jsonPath("$.data.carts[0].originalPrice").value(30000))
            .andExpect(jsonPath("$.data.carts[0].discountPrice").value(30000))
            .andExpect(jsonPath("$.data.carts[0].discountRate").isEmpty())
            .andDo(document("cart/get-carts-first-page",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 조회")
                    .description("로그인한 회원의 장바구니를 커서 기반 페이지네이션으로 조회합니다. cursor 생략 시 최신 항목부터 반환합니다.")
                    .responseSchema(Schema.schema("CartPageResponse"))
                    .queryParameters(
                        parameterWithName("cursor").description("마지막으로 조회한 cartId (첫 페이지 요청 시 생략)").optional(),
                        parameterWithName("size").description("페이지 크기 (기본값: 8)").optional()
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.carts").type(JsonFieldType.ARRAY).description("장바구니 항목 목록"),
                        fieldWithPath("data.carts[].cartId").type(JsonFieldType.NUMBER).description("장바구니 항목 ID"),
                        fieldWithPath("data.carts[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                        fieldWithPath("data.carts[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.carts[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.carts[].discountPrice").type(JsonFieldType.NUMBER).description("할인 적용가 (할인 없으면 정가와 동일)"),
                        fieldWithPath("data.carts[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 (%) - 할인 없으면 null"),
                        fieldWithPath("data.carts[].color").type(JsonFieldType.STRING).description("색상"),
                        fieldWithPath("data.carts[].size").type(JsonFieldType.STRING).description("사이즈"),
                        fieldWithPath("data.carts[].mainImageUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                        fieldWithPath("data.nextCursor").type(JsonFieldType.VARIES).optional().description("다음 페이지 요청 시 사용할 cursor 값 (마지막 페이지면 null)"),
                        fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/carts - 마지막 페이지 조회 성공 (cursor 있음, hasNext=false)")
    void getCarts_lastPage_success() throws Exception {
        // given - 3개만 반환 → hasNext=false, nextCursor=null
        List<Carts> mockResult = mockCartsNoDiscount(3, 3L); // id: 3,2,1
        given(cartService.getCarts(anyLong(), anyLong(), anyInt())).willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/v1/carts?cursor=4")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.carts.length()").value(3))
            .andExpect(jsonPath("$.data.hasNext").value(false))
            .andExpect(jsonPath("$.data.nextCursor").isEmpty())
            .andDo(document("cart/get-carts-last-page",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 조회 - 마지막 페이지")
                    .description("cursor를 전달하여 이전 페이지 이후 항목을 조회합니다. hasNext가 false이면 더 이상 데이터가 없습니다.")
                    .responseSchema(Schema.schema("CartPageResponse"))
                    .queryParameters(
                        parameterWithName("cursor").description("마지막으로 조회한 cartId").optional(),
                        parameterWithName("size").description("페이지 크기 (기본값: 8)").optional()
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.carts").type(JsonFieldType.ARRAY).description("장바구니 항목 목록"),
                        fieldWithPath("data.carts[].cartId").type(JsonFieldType.NUMBER).description("장바구니 항목 ID"),
                        fieldWithPath("data.carts[].quantity").type(JsonFieldType.NUMBER).description("수량"),
                        fieldWithPath("data.carts[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.carts[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.carts[].discountPrice").type(JsonFieldType.NUMBER).description("할인 적용가"),
                        fieldWithPath("data.carts[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 (%)"),
                        fieldWithPath("data.carts[].color").type(JsonFieldType.STRING).description("색상"),
                        fieldWithPath("data.carts[].size").type(JsonFieldType.STRING).description("사이즈"),
                        fieldWithPath("data.carts[].mainImageUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                        fieldWithPath("data.nextCursor").type(JsonFieldType.VARIES).optional().description("다음 페이지 cursor (null)"),
                        fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/carts - 상시 할인 상품이 포함된 경우 discountPrice 계산 검증")
    void getCarts_withAlwaysDiscount_success() throws Exception {
        // given - ALWAYS 10% 할인: floor(30000 * 90 / 100.0) = 27000
        List<Carts> mockResult = List.of(mockCartAlwaysDiscount(1L, 2));
        given(cartService.getCarts(anyLong(), any(), anyInt())).willReturn(mockResult);

        // when & then
        mockMvc.perform(get("/v1/carts")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.carts[0].originalPrice").value(30000))
            .andExpect(jsonPath("$.data.carts[0].discountPrice").value(27000))
            .andExpect(jsonPath("$.data.carts[0].discountRate").value(10))
            .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("GET /v1/carts - 장바구니가 비어있는 경우 빈 목록 반환")
    void getCarts_emptyCart_success() throws Exception {
        // given
        given(cartService.getCarts(anyLong(), any(), anyInt())).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/v1/carts")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.carts.length()").value(0))
            .andExpect(jsonPath("$.data.hasNext").value(false))
            .andExpect(jsonPath("$.data.nextCursor").isEmpty());
    }

    @Test
    @DisplayName("GET /v1/carts - 인증 없이 접근 시 401 반환")
    void getCarts_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/carts")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== POST /v1/carts ========================

    @Test
    @DisplayName("POST /v1/carts - 장바구니 추가 성공 (단건)")
    void saveCarts_singleItem_success() throws Exception {
        // given
        List<CartSaveRequest> requests = List.of(new CartSaveRequest(1L, 2));
        willDoNothing().given(cartService).saveCarts(anyLong(), anyList());

        // when & then
        mockMvc.perform(post("/v1/carts")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("장바구니 추가에 성공하였습니다."))
            .andDo(document("cart/save-carts",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 추가")
                    .description("장바구니에 상품을 추가합니다. 이미 담긴 상품이면 수량을 합산합니다. 단건 또는 다건 모두 리스트 형태로 전달합니다.")
                    .requestSchema(Schema.schema("CartSaveRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestFields(
                        fieldWithPath("[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID (색상/사이즈 조합)"),
                        fieldWithPath("[].quantity").type(JsonFieldType.NUMBER).description("수량 (최소 1)")
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
    @DisplayName("POST /v1/carts - 장바구니 추가 성공 (다건)")
    void saveCarts_multipleItems_success() throws Exception {
        // given
        List<CartSaveRequest> requests = List.of(
            new CartSaveRequest(1L, 2),
            new CartSaveRequest(2L, 1),
            new CartSaveRequest(3L, 3)
        );
        willDoNothing().given(cartService).saveCarts(anyLong(), anyList());

        // when & then
        mockMvc.perform(post("/v1/carts")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andDo(document("cart/save-carts-multiple",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 추가 - 다건")
                    .description("여러 상품을 한 번에 장바구니에 추가합니다.")
                    .requestSchema(Schema.schema("CartSaveRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestFields(
                        fieldWithPath("[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("[].quantity").type(JsonFieldType.NUMBER).description("수량 (최소 1)")
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
    @DisplayName("POST /v1/carts - 수량이 0 이하인 경우 400 반환")
    void saveCarts_invalidQuantity() throws Exception {
        // given
        List<CartSaveRequest> requests = List.of(new CartSaveRequest(1L, 0));

        // when & then
        mockMvc.perform(post("/v1/carts")
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("cart/save-carts-invalid-quantity",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 추가 - 수량 오류")
                    .description("수량이 1 미만인 경우 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID"),
                        fieldWithPath("[].quantity").type(JsonFieldType.NUMBER).description("수량 (0 이하 - 오류)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)"),
                        fieldWithPath("errors").type(JsonFieldType.ARRAY).optional().description("필드 유효성 검증 오류 목록"),
                        fieldWithPath("errors[].field").type(JsonFieldType.STRING).optional().description("오류 필드명"),
                        fieldWithPath("errors[].message").type(JsonFieldType.STRING).optional().description("오류 메시지")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/carts - 인증 없이 접근 시 401 반환")
    void saveCarts_unauthorized() throws Exception {
        List<CartSaveRequest> requests = List.of(new CartSaveRequest(1L, 1));

        mockMvc.perform(post("/v1/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== PATCH /v1/carts/{cartId} ========================

    @Test
    @DisplayName("PATCH /v1/carts/{cartId} - 장바구니 수정 성공")
    void updateCarts_success() throws Exception {
        // given
        CartUpdate request = new CartUpdate(2L, 3);
        willDoNothing().given(cartService).updateCarts(anyLong(), anyLong(), anyLong(), any(Integer.class));

        // when & then
        mockMvc.perform(patch("/v1/carts/{cartId}", 1L)
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("장바구니 수정에 성공하였습니다."))
            .andDo(document("cart/update-cart",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 수정")
                    .description("장바구니 항목의 색상/사이즈(productDetailId)와 수량을 수정합니다.")
                    .requestSchema(Schema.schema("CartUpdateRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .pathParameters(
                        parameterWithName("cartId").description("수정할 장바구니 항목 ID")
                    )
                    .requestFields(
                        fieldWithPath("productDetailsId").type(JsonFieldType.NUMBER).description("변경할 상품 상세 ID (색상/사이즈 조합)"),
                        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("변경할 수량 (최소 1)")
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
    @DisplayName("PATCH /v1/carts/{cartId} - 존재하지 않는 카트 404 반환")
    void updateCarts_cartNotFound() throws Exception {
        // given
        CartUpdate request = new CartUpdate(2L, 3);
        willThrow(new BusinessException(UserErrorCode.CART_NOT_FOUND))
            .given(cartService).updateCarts(anyLong(), anyLong(), anyLong(), any(Integer.class));

        // when & then
        mockMvc.perform(patch("/v1/carts/{cartId}", 999L)
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("존재하지 않는 카트 번호입니다."))
            .andDo(document("cart/update-cart-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 수정 - 카트 없음")
                    .description("존재하지 않거나 본인 소유가 아닌 장바구니 항목 수정 시 404를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(
                        parameterWithName("cartId").description("수정할 장바구니 항목 ID")
                    )
                    .requestFields(
                        fieldWithPath("productDetailsId").type(JsonFieldType.NUMBER).description("변경할 상품 상세 ID"),
                        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("변경할 수량")
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
    @DisplayName("PATCH /v1/carts/{cartId} - 인증 없이 접근 시 401 반환")
    void updateCarts_unauthorized() throws Exception {
        CartUpdate request = new CartUpdate(2L, 3);

        mockMvc.perform(patch("/v1/carts/{cartId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== DELETE /v1/carts?ids= ========================

    @Test
    @DisplayName("DELETE /v1/carts?ids= - 선택 삭제 성공")
    void deleteCarts_success() throws Exception {
        // given
        willDoNothing().given(cartService).deleteCartsByCartsId(anyList(), anyLong());

        // when & then
        mockMvc.perform(delete("/v1/carts?ids=1&ids=2&ids=3")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("장바구니 삭제에 성공하였습니다."))
            .andDo(document("cart/delete-carts",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 선택 삭제")
                    .description("선택한 장바구니 항목들을 삭제합니다. 본인 소유 항목만 삭제됩니다.")
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .queryParameters(
                        parameterWithName("ids").description("삭제할 장바구니 항목 ID 목록 (1개 이상)")
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
    @DisplayName("DELETE /v1/carts?ids= - 인증 없이 접근 시 401 반환")
    void deleteCarts_unauthorized() throws Exception {
        mockMvc.perform(delete("/v1/carts")
                .param("ids", "1", "2")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== DELETE /v1/carts/all ========================

    @Test
    @DisplayName("DELETE /v1/carts/all - 전체 삭제 성공")
    void deleteAllCarts_success() throws Exception {
        // given
        willDoNothing().given(cartService).deleteAllCartsByUser(anyLong());

        // when & then
        mockMvc.perform(delete("/v1/carts/all")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("장바구니 삭제에 성공하였습니다."))
            .andDo(document("cart/delete-all-carts",
                resource(ResourceSnippetParameters.builder()
                    .tag("Cart")
                    .summary("장바구니 전체 삭제")
                    .description("로그인한 회원의 장바구니를 전체 삭제합니다.")
                    .responseSchema(Schema.schema("SuccessResponse"))
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
    @DisplayName("DELETE /v1/carts/all - 인증 없이 접근 시 401 반환")
    void deleteAllCarts_unauthorized() throws Exception {
        mockMvc.perform(delete("/v1/carts/all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
