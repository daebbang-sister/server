package com.daebbang.daebbangapi.domain.wish.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangapi.domain.wish.dto.request.WishListSaveRequest;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import com.daebbang.daebbangcore.domain.wish.dto.WishListQueryResult;
import com.daebbang.daebbangcore.domain.wish.service.WishListService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(controllers = WishListController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class WishListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WishListService wishListService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    private static final String BASE_URL = "/v1/wish-lists";
    private static final Long USER_ID = 1L;

    private UsernamePasswordAuthenticationToken authToken() {
        return new UsernamePasswordAuthenticationToken(
            USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    /**
     * DiscountType.NONE - 할인 없는 위시리스트 항목
     * DiscountCalculator.calculate() → DiscountResult.noDiscount(originalPrice)
     * sellingPrice = originalPrice, discountRate = null
     */
    private WishListQueryResult mockResultNoDiscount(Long wishListId, Long productId) {
        return new WishListQueryResult(
            wishListId,
            productId,
            "https://example.com/image.jpg",
            "테스트 상품",
            30000,
            DiscountType.NONE,
            null,
            null,
            null
        );
    }

    /**
     * DiscountType.ALWAYS - 상시 10% 할인
     * sellingPrice = floor(50000 * 90 / 100.0) = 45000, discountRate = 10
     */
    private WishListQueryResult mockResultAlwaysDiscount(Long wishListId, Long productId) {
        return new WishListQueryResult(
            wishListId,
            productId,
            "https://example.com/sale-image.jpg",
            "할인 상품",
            50000,
            DiscountType.ALWAYS,
            10,
            null,
            null
        );
    }

    // ======================== GET /v1/wish-lists ========================

    @Test
    @DisplayName("GET /v1/wish-lists - 위시리스트 조회 성공")
    void getWishList_success() throws Exception {
        // given
        List<WishListQueryResult> results = List.of(
            mockResultNoDiscount(2L, 100L),
            mockResultAlwaysDiscount(1L, 200L)
        );
        given(wishListService.getWishList(anyLong(), any()))
            .willReturn(new PageImpl<>(results, PageRequest.of(0, 10), 2));

        // when & then
        mockMvc.perform(get(BASE_URL)
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].wishListId").value(2))
            .andExpect(jsonPath("$.data.content[0].productId").value(100))
            .andExpect(jsonPath("$.data.content[0].productName").value("테스트 상품"))
            .andExpect(jsonPath("$.data.content[0].originalPrice").value(30000))
            .andExpect(jsonPath("$.data.content[0].sellingPrice").value(30000))
            .andExpect(jsonPath("$.data.content[0].discountRate").isEmpty())
            .andExpect(jsonPath("$.data.content[1].sellingPrice").value(45000))
            .andExpect(jsonPath("$.data.content[1].discountRate").value(10))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andDo(document("wish-lists/list-01-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 조회")
                    .description("로그인한 회원의 위시리스트를 페이지네이션으로 조회합니다. 최신 추가 순으로 정렬됩니다.")
                    .responseSchema(Schema.schema("WishListPageResponse"))
                    .queryParameters(
                        parameterWithName("page").description("페이지 번호 (0부터 시작, 기본값: 0)").optional(),
                        parameterWithName("size").description("페이지 크기 (기본값: 20)").optional()
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("위시리스트 항목 목록"),
                        fieldWithPath("data.content[].wishListId").type(JsonFieldType.NUMBER).description("위시리스트 항목 ID (삭제 시 사용)"),
                        fieldWithPath("data.content[].productId").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data.content[].mainImageUrl").type(JsonFieldType.STRING).description("대표 이미지 URL"),
                        fieldWithPath("data.content[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.content[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.content[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 (%) - 할인 없으면 null"),
                        fieldWithPath("data.content[].sellingPrice").type(JsonFieldType.NUMBER).description("판매가 (할인 없으면 정가와 동일)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 항목 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/wish-lists - 위시리스트가 비어있는 경우 빈 목록 반환")
    void getWishList_empty() throws Exception {
        // given
        given(wishListService.getWishList(anyLong(), any()))
            .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        // when & then
        mockMvc.perform(get(BASE_URL)
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content.length()").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andDo(document("wish-lists/list-02-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 조회 - 빈 목록")
                    .description("위시리스트가 비어있는 경우 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("WishListPageResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("위시리스트 항목 목록 (빈 배열)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 항목 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/wish-lists - 인증 없이 접근 시 401 반환")
    void getWishList_unauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andDo(document("wish-lists/list-03-unauthorized",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 조회 - 인증 실패")
                    .description("인증 토큰이 없거나 만료된 경우 401을 반환합니다. 모든 위시리스트 API는 로그인이 필요합니다.")
                    .build()
                )));
    }

    // ======================== GET /v1/wish-lists/check ========================

    @Test
    @DisplayName("GET /v1/wish-lists/check - 위시리스트 등록 상품이면 true 반환")
    void checkWishList_wished() throws Exception {
        given(wishListService.isWished(anyLong(), anyLong())).willReturn(true);

        mockMvc.perform(get(BASE_URL + "/check")
                .with(authentication(authToken()))
                .param("productId", "100")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("위시리스트 여부 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data").value(true))
            .andDo(document("wish-lists/check-01-wished",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 여부 조회")
                    .description("상품 상세 페이지 진입 후 CSR로 위시리스트 등록 여부를 조회합니다. 등록된 경우 true, 아니면 false를 반환합니다.")
                    .responseSchema(Schema.schema("WishListCheckResponse"))
                    .queryParameters(
                        parameterWithName("productId").description("조회할 상품 ID")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("위시리스트 등록 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/wish-lists/check - 미등록 상품이면 false 반환")
    void checkWishList_notWished() throws Exception {
        given(wishListService.isWished(anyLong(), anyLong())).willReturn(false);

        mockMvc.perform(get(BASE_URL + "/check")
                .with(authentication(authToken()))
                .param("productId", "999")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(false))
            .andDo(document("wish-lists/check-02-not-wished",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 여부 조회 - 미등록")
                    .description("위시리스트에 등록되지 않은 상품은 false를 반환합니다.")
                    .responseSchema(Schema.schema("WishListCheckResponse"))
                    .queryParameters(
                        parameterWithName("productId").description("조회할 상품 ID")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("위시리스트 등록 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/wish-lists/check - productId가 0 이하이면 400 반환")
    void checkWishList_invalidProductId() throws Exception {
        mockMvc.perform(get(BASE_URL + "/check")
                .with(authentication(authToken()))
                .param("productId", "0")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /v1/wish-lists/check - 인증 없이 접근 시 401 반환")
    void checkWishList_unauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/check")
                .param("productId", "100")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== POST /v1/wish-lists ========================

    @Test
    @DisplayName("POST /v1/wish-lists - 위시리스트 추가 성공")
    void addWishList_success() throws Exception {
        // given
        WishListSaveRequest request = new WishListSaveRequest(100L);
        given(wishListService.addWishList(anyLong(), anyLong())).willReturn(1L);

        // when & then
        mockMvc.perform(post(BASE_URL)
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("위시리스트 추가에 성공하였습니다."))
            .andExpect(jsonPath("$.data.wishId").value(1))
            .andDo(document("wish-lists/add-01-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 추가")
                    .description("상품 상세 페이지에서 상품을 위시리스트에 추가합니다. 이미 추가된 상품은 409를 반환합니다.")
                    .requestSchema(Schema.schema("WishListSaveRequest"))
                    .responseSchema(Schema.schema("WishIdResponse"))
                    .requestFields(
                        fieldWithPath("productId").type(JsonFieldType.NUMBER).description("추가할 상품 ID")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.wishId").type(JsonFieldType.NUMBER).description("생성된 위시리스트 ID (삭제 시 사용)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/wish-lists - 이미 추가된 상품 409 반환")
    void addWishList_alreadyExists() throws Exception {
        // given
        WishListSaveRequest request = new WishListSaveRequest(100L);
        willThrow(new BusinessException(UserErrorCode.WISH_LIST_ALREADY_EXISTS))
            .given(wishListService).addWishList(anyLong(), anyLong());

        // when & then
        mockMvc.perform(post(BASE_URL)
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("이미 위시리스트에 추가된 상품입니다."))
            .andDo(document("wish-lists/add-02-already-exists",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 추가 - 중복")
                    .description("이미 위시리스트에 추가된 상품을 재추가 시 409를 반환합니다.")
                    .requestSchema(Schema.schema("WishListSaveRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("productId").type(JsonFieldType.NUMBER).description("추가할 상품 ID")
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
    @DisplayName("POST /v1/wish-lists - 존재하지 않는 상품 404 반환")
    void addWishList_productNotFound() throws Exception {
        // given
        WishListSaveRequest request = new WishListSaveRequest(999L);
        willThrow(new BusinessException(UserErrorCode.PRODUCT_NOT_FOUND))
            .given(wishListService).addWishList(anyLong(), anyLong());

        // when & then
        mockMvc.perform(post(BASE_URL)
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."));
    }

    @Test
    @DisplayName("POST /v1/wish-lists - productId 누락 시 400 반환")
    void addWishList_missingProductId() throws Exception {
        // given - productId가 null인 요청
        String invalidBody = "{}";

        // when & then
        mockMvc.perform(post(BASE_URL)
                .with(authentication(authToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andExpect(jsonPath("$.errors[0].field").value("productId"))
            .andExpect(jsonPath("$.errors[0].message").value("상품 ID는 필수입니다."));
    }

    @Test
    @DisplayName("POST /v1/wish-lists - 인증 없이 접근 시 401 반환")
    void addWishList_unauthorized() throws Exception {
        WishListSaveRequest request = new WishListSaveRequest(100L);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== DELETE /v1/wish-lists?ids= ========================

    @Test
    @DisplayName("DELETE /v1/wish-lists?ids= - 선택 삭제 성공")
    void deleteWishLists_success() throws Exception {
        // given
        willDoNothing().given(wishListService).deleteWishLists(anyLong(), anyList());

        // when & then
        mockMvc.perform(delete(BASE_URL + "?ids=1&ids=2&ids=3")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("위시리스트 삭제에 성공하였습니다."))
            .andDo(document("wish-lists/delete-01-selected",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 선택 삭제")
                    .description("선택한 위시리스트 항목들을 삭제합니다. 본인 소유 항목만 삭제되며, 단건 삭제도 동일하게 처리합니다.")
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .queryParameters(
                        parameterWithName("ids").description("삭제할 위시리스트 항목 ID 목록 (반복 파라미터, 예: ?ids=1&ids=2, 1개 이상)")
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
    @DisplayName("DELETE /v1/wish-lists - ids 파라미터 없을 시 400 반환")
    void deleteWishLists_missingIds() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andExpect(jsonPath("$.errors[0].field").value("ids"));
    }

    @Test
    @DisplayName("DELETE /v1/wish-lists?ids= - 인증 없이 접근 시 401 반환")
    void deleteWishLists_unauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL)
                .param("ids", "1", "2")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ======================== DELETE /v1/wish-lists/all ========================

    @Test
    @DisplayName("DELETE /v1/wish-lists/all - 전체 삭제 성공")
    void deleteAllWishLists_success() throws Exception {
        // given
        willDoNothing().given(wishListService).deleteAllWishLists(anyLong());

        // when & then
        mockMvc.perform(delete(BASE_URL + "/all")
                .with(authentication(authToken()))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("위시리스트 삭제에 성공하였습니다."))
            .andDo(document("wish-lists/delete-02-all",
                resource(ResourceSnippetParameters.builder()
                    .tag("WishList")
                    .summary("위시리스트 전체 삭제")
                    .description("로그인한 회원의 위시리스트를 전체 삭제합니다.")
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
    @DisplayName("DELETE /v1/wish-lists/all - 인증 없이 접근 시 401 반환")
    void deleteAllWishLists_unauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/all")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
