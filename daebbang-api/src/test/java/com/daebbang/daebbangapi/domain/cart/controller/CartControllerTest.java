package com.daebbang.daebbangapi.domain.cart.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.daebbang.daebbangcore.domain.cart.service.CartService;
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
