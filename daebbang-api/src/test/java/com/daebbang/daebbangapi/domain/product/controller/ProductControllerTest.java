package com.daebbang.daebbangapi.domain.product.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductColorOption;
import com.daebbang.daebbangcore.domain.product.dto.ProductDetailResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductGalleryImageResult;
import com.daebbang.daebbangcore.domain.product.dto.ProductSizeOption;
import com.daebbang.daebbangcore.domain.product.entity.DiscountType;
import com.daebbang.daebbangcore.domain.product.entity.ProductStatus;
import com.daebbang.daebbangcore.domain.product.service.ProductService;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import com.daebbang.daebbangcommon.sort.SortDirection;
import com.daebbang.daebbangcore.domain.product.entity.ProductSortType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private com.daebbang.daebbangcore.domain.review.service.ReviewService reviewService;

    private static final String BASE_URL = "/v1/products";
    private static final Long USER_ID = 1L;

    private UsernamePasswordAuthenticationToken authToken() {
        return new UsernamePasswordAuthenticationToken(
            USER_ID, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private PageImpl<ProductCardQueryResult> createProductPage() {
        return new PageImpl<>(createProductQueryResults(), PageRequest.of(0, 8), 2L);
    }

    private List<ProductCardQueryResult> createProductQueryResults() {
        return List.of(
            new ProductCardQueryResult(
                1L,
                "TOP",
                "루즈핏 티셔츠",
                "http://example.com/main1.jpg",
                "http://example.com/hover1.jpg",
                50000,
                DiscountType.PERIOD,
                20,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                ProductStatus.SALE,
                List.of("#000000", "#FFFFFF")
            ),
            new ProductCardQueryResult(
                2L,
                "TOP",
                "오버핏 니트",
                "http://example.com/main2.jpg",
                null,
                39000,
                DiscountType.NONE,
                null,
                null,
                null,
                ProductStatus.SALE,
                List.of("#8B5A2B")
            )
        );
    }

    @Test
    @DisplayName("GET /v1/products/main/new - 신상품 조회 성공")
    void getMainNewProductsOnSale_success() throws Exception {
        // given
        given(productService.getOnSaleNewProducts(anyInt()))
            .willReturn(createProductQueryResults());

        // when & then
        mockMvc.perform(get(BASE_URL + "/main/new")
                .param("limit", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1L))
            .andExpect(jsonPath("$.data[0].categoryName").value("TOP"))
            .andExpect(jsonPath("$.data[0].productName").value("루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data[0].originalPrice").value(50000))
            .andExpect(jsonPath("$.data[0].sellingPrice").value(40000))
            .andExpect(jsonPath("$.data[0].discountRate").value(20))
            .andExpect(jsonPath("$.data[0].colorCodes").isArray())
            .andExpect(jsonPath("$.data[0].colorCodes[0]").value("#000000"))
            .andExpect(jsonPath("$.data[1].id").value(2L))
            .andExpect(jsonPath("$.data[1].sellingPrice").value(39000))
            .andExpect(jsonPath("$.data[1].discountRate").doesNotExist())
            .andExpect(jsonPath("$.data[1].colorCodes[0]").value("#8B5A2B"))
            .andDo(document("products/main-new",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("메인 신상품 조회")
                    .description("메인 페이지 신상품 섹션 상품 목록을 조회합니다. 등록일 기준 최신순으로 반환되며, 할인 중인 상품은 할인가와 할인율이 함께 반환됩니다.")
                    .responseSchema(Schema.schema("ProductMainCardListResponse"))
                    .queryParameters(
                        parameterWithName("limit").description("조회할 상품 수").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("신상품 목록"),
                        fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data[].categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("data[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data[].mainImageUrl").type(JsonFieldType.STRING).description("메인 이미지 URL"),
                        fieldWithPath("data[].hoverImageUrl").type(JsonFieldType.VARIES).optional().description("호버 이미지 URL (없을 수 있음)"),
                        fieldWithPath("data[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data[].sellingPrice").type(JsonFieldType.NUMBER).description("판매가 (할인 미적용 시 정가와 동일)"),
                        fieldWithPath("data[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)"),
                        fieldWithPath("data[].colorCodes").type(JsonFieldType.ARRAY).description("색상 코드 목록 (HEX 문자열 배열, 예: [\"#000000\", \"#FFFFFF\"])")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/main/new - 신상품이 없는 경우 빈 배열 반환")
    void getMainNewProductsOnSale_empty() throws Exception {
        // given
        given(productService.getOnSaleNewProducts(anyInt()))
            .willReturn(List.of());

        // when & then
        mockMvc.perform(get(BASE_URL + "/main/new")
                .param("limit", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andDo(document("products/main-new-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("메인 신상품 조회 - 빈 목록")
                    .description("등록된 신상품이 없는 경우 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("ProductMainCardListResponse"))
                    .queryParameters(
                        parameterWithName("limit").description("조회할 상품 수").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("신상품 목록 (빈 배열)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/main/category/{categoryId} - 카테고리별 상품 조회 성공")
    void getMainCategoryProductsOnSale_success() throws Exception {
        // given
        given(productService.getOnSaleCategoryProducts(anyLong(), anyInt()))
            .willReturn(createProductQueryResults());

        // when & then
        mockMvc.perform(get(BASE_URL + "/main/category/{categoryId}", 1L)
                .param("limit", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].id").value(1L))
            .andExpect(jsonPath("$.data[0].categoryName").value("TOP"))
            .andExpect(jsonPath("$.data[0].productName").value("루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data[0].originalPrice").value(50000))
            .andExpect(jsonPath("$.data[0].sellingPrice").value(40000))
            .andExpect(jsonPath("$.data[0].discountRate").value(20))
            .andExpect(jsonPath("$.data[0].colorCodes").isArray())
            .andExpect(jsonPath("$.data[0].colorCodes[0]").value("#000000"))
            .andDo(document("products/main-category",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("메인 카테고리별 상품 조회")
                    .pathParameters(
                        parameterWithName("categoryId").description("카테고리 ID").type(SimpleType.INTEGER)
                    )
                    .description("메인 페이지 카테고리별 상품 목록을 조회합니다. 등록일 기준 최신순으로 반환됩니다.")
                    .responseSchema(Schema.schema("ProductMainCardListResponse"))
                    .queryParameters(
                        parameterWithName("limit").description("조회할 상품 수").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("카테고리 상품 목록"),
                        fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data[].categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("data[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data[].mainImageUrl").type(JsonFieldType.STRING).description("메인 이미지 URL"),
                        fieldWithPath("data[].hoverImageUrl").type(JsonFieldType.VARIES).optional().description("호버 이미지 URL (없을 수 있음)"),
                        fieldWithPath("data[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data[].sellingPrice").type(JsonFieldType.NUMBER).description("판매가 (할인 미적용 시 정가와 동일)"),
                        fieldWithPath("data[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)"),
                        fieldWithPath("data[].colorCodes").type(JsonFieldType.ARRAY).description("색상 코드 목록 (HEX 문자열 배열, 예: [\"#000000\", \"#FFFFFF\"])")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/new - 신상품 전체 페이지 조회 성공")
    void getNewProductsOnSale_success() throws Exception {
        // given
        given(productService.getOnSaleProductsByCategory(
            isNull(), any(ProductSortType.class), any(SortDirection.class), any(Pageable.class)))
            .willReturn(createProductPage());

        // when & then
        mockMvc.perform(get(BASE_URL + "/new")
                .param("direction", "DESC")
                .param("page", "0")
                .param("size", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content[0].id").value(1L))
            .andExpect(jsonPath("$.data.content[0].categoryName").value("TOP"))
            .andExpect(jsonPath("$.data.content[0].productName").value("루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data.content[0].originalPrice").value(50000))
            .andExpect(jsonPath("$.data.content[0].sellingPrice").value(40000))
            .andExpect(jsonPath("$.data.content[0].discountRate").value(20))
            .andExpect(jsonPath("$.data.content[0].colorCodes").isArray())
            .andExpect(jsonPath("$.data.content[0].colorCodes[0]").value("#000000"))
            .andExpect(jsonPath("$.data.content[1].sellingPrice").value(39000))
            .andExpect(jsonPath("$.data.content[1].discountRate").doesNotExist())
            .andExpect(jsonPath("$.data.content[1].colorCodes[0]").value("#8B5A2B"))
            .andExpect(jsonPath("$.data.pageNumber").value(0))
            .andExpect(jsonPath("$.data.pageSize").value(8))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.last").value(true))
            .andDo(document("products/new",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("신상품 전체 페이지 조회")
                    .description("신상품 전체 페이지를 조회합니다. 등록일 기준 최신순으로 반환되며 페이징이 적용됩니다.")
                    .responseSchema(Schema.schema("ProductPageResponse"))
                    .queryParameters(
                        parameterWithName("direction").description("정렬 방향 (ASC: 오래된순, DESC: 최신순) - 기본값: DESC").type(SimpleType.STRING),
                        parameterWithName("page").description("페이지 번호 (0부터 시작)").type(SimpleType.INTEGER),
                        parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("신상품 목록"),
                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data.content[].categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("data.content[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.content[].mainImageUrl").type(JsonFieldType.STRING).description("메인 이미지 URL"),
                        fieldWithPath("data.content[].hoverImageUrl").type(JsonFieldType.VARIES).optional().description("호버 이미지 URL (없을 수 있음)"),
                        fieldWithPath("data.content[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.content[].sellingPrice").type(JsonFieldType.NUMBER).description("판매가 (할인 미적용 시 정가와 동일)"),
                        fieldWithPath("data.content[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)"),
                        fieldWithPath("data.content[].colorCodes").type(JsonFieldType.ARRAY).description("색상 코드 목록 (HEX 문자열 배열, 예: [\"#000000\", \"#FFFFFF\"])"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 상품 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/new - 신상품이 없는 경우 빈 목록 반환")
    void getNewProductsOnSale_empty() throws Exception {
        // given
        given(productService.getOnSaleProductsByCategory(
            isNull(), any(ProductSortType.class), any(SortDirection.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 8), 0L));

        // when & then
        mockMvc.perform(get(BASE_URL + "/new")
                .param("page", "0")
                .param("size", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andDo(document("products/new-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("신상품 전체 페이지 조회 - 빈 목록")
                    .description("등록된 신상품이 없는 경우 빈 목록을 반환합니다.")
                    .responseSchema(Schema.schema("ProductPageResponse"))
                    .queryParameters(
                        parameterWithName("page").description("페이지 번호").type(SimpleType.INTEGER),
                        parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("신상품 목록 (빈 배열)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 상품 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/category/{categoryId} - 카테고리별 상품 전체 페이지 조회 성공")
    void getCategoryProductsOnSale_success() throws Exception {
        // given
        given(productService.getOnSaleProductsByCategory(
            anyLong(), any(ProductSortType.class), any(SortDirection.class), any(Pageable.class)))
            .willReturn(createProductPage());

        // when & then
        mockMvc.perform(get(BASE_URL + "/category/{categoryId}", 1L)
                .param("sortType", "NEW")
                .param("direction", "DESC")
                .param("page", "0")
                .param("size", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content[0].id").value(1L))
            .andExpect(jsonPath("$.data.content[0].categoryName").value("TOP"))
            .andExpect(jsonPath("$.data.content[0].productName").value("루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data.content[0].originalPrice").value(50000))
            .andExpect(jsonPath("$.data.content[0].sellingPrice").value(40000))
            .andExpect(jsonPath("$.data.content[0].discountRate").value(20))
            .andExpect(jsonPath("$.data.content[0].colorCodes").isArray())
            .andExpect(jsonPath("$.data.content[0].colorCodes[0]").value("#000000"))
            .andExpect(jsonPath("$.data.content[1].sellingPrice").value(39000))
            .andExpect(jsonPath("$.data.content[1].discountRate").doesNotExist())
            .andExpect(jsonPath("$.data.content[1].colorCodes[0]").value("#8B5A2B"))
            .andExpect(jsonPath("$.data.pageNumber").value(0))
            .andExpect(jsonPath("$.data.pageSize").value(8))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andExpect(jsonPath("$.data.totalPages").value(1))
            .andExpect(jsonPath("$.data.last").value(true))
            .andDo(document("products/category",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("카테고리별 상품 전체 페이지 조회")
                    .description("카테고리별 상품 전체 페이지를 조회합니다. 정렬 기준과 방향을 지정할 수 있으며 기본값은 신상품순(최신순)입니다.")
                    .responseSchema(Schema.schema("ProductPageResponse"))
                    .pathParameters(
                        parameterWithName("categoryId").description("카테고리 ID").type(SimpleType.INTEGER)
                    )
                    .queryParameters(
                        parameterWithName("sortType").description("정렬 기준 (NEW: 신상품순, LATEST: 최신순, NAME: 상품명순, PRICE: 가격순, POPULAR: 인기순) - 기본값: NEW").type(SimpleType.STRING),
                        parameterWithName("direction").description("정렬 방향 (ASC: 오름차순, DESC: 내림차순) - 기본값: DESC").type(SimpleType.STRING),
                        parameterWithName("page").description("페이지 번호 (0부터 시작)").type(SimpleType.INTEGER),
                        parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("상품 목록"),
                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data.content[].categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("data.content[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.content[].mainImageUrl").type(JsonFieldType.STRING).description("메인 이미지 URL"),
                        fieldWithPath("data.content[].hoverImageUrl").type(JsonFieldType.VARIES).optional().description("호버 이미지 URL (없을 수 있음)"),
                        fieldWithPath("data.content[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.content[].sellingPrice").type(JsonFieldType.NUMBER).description("판매가 (할인 미적용 시 정가와 동일)"),
                        fieldWithPath("data.content[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)"),
                        fieldWithPath("data.content[].colorCodes").type(JsonFieldType.ARRAY).description("색상 코드 목록 (HEX 문자열 배열, 예: [\"#000000\", \"#FFFFFF\"])"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 상품 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/main/category/{categoryId} - 해당 카테고리 상품이 없는 경우 빈 배열 반환")
    void getMainCategoryProductsOnSale_empty() throws Exception {
        // given
        given(productService.getOnSaleCategoryProducts(anyLong(), anyInt()))
            .willReturn(List.of());

        // when & then
        mockMvc.perform(get(BASE_URL + "/main/category/{categoryId}", 999L)
                .param("limit", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andDo(document("products/main-category-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("메인 카테고리별 상품 조회 - 빈 목록")
                    .description("해당 카테고리에 상품이 없는 경우 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("ProductMainCardListResponse"))
                    .queryParameters(
                        parameterWithName("limit").description("조회할 상품 수").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("카테고리 상품 목록 (빈 배열)")
                    )
                    .build()
                )));
    }

    private ProductDetailResult createProductDetailResult() {
        return new ProductDetailResult(
            1L,
            "TOP",
            "루즈핏 티셔츠",
            "편안한 루즈핏 티셔츠",
            "http://example.com/main1.jpg",
            50000,
            DiscountType.PERIOD,
            20,
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(5),
            "<p>상품 상세 설명</p>",
            List.of(
                new ProductGalleryImageResult("http://example.com/detail1.jpg", (short) 1),
                new ProductGalleryImageResult("http://example.com/detail2.jpg", (short) 2)
            ),
            List.of(
                new ProductColorOption("블랙", "#000000", List.of(
                    new ProductSizeOption(1L, "S", 10, false),
                    new ProductSizeOption(2L, "M", 0, true)
                )),
                new ProductColorOption("화이트", "#FFFFFF", List.of(
                    new ProductSizeOption(3L, "S", 5, false)
                ))
            )
        );
    }

    @Test
    @DisplayName("GET /v1/products/{productId} - 상품 상세 조회 성공")
    void getProductDetail_success() throws Exception {
        given(productService.getProductDetail(anyLong())).willReturn(createProductDetailResult());

        mockMvc.perform(get(BASE_URL + "/{productId}", 1L)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.categoryName").value("TOP"))
            .andExpect(jsonPath("$.data.productName").value("루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data.simpleDescription").value("편안한 루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data.mainImageUrl").value("http://example.com/main1.jpg"))
            .andExpect(jsonPath("$.data.originalPrice").value(50000))
            .andExpect(jsonPath("$.data.sellingPrice").value(40000))
            .andExpect(jsonPath("$.data.discountRate").value(20))
            .andExpect(jsonPath("$.data.gallery").isArray())
            .andExpect(jsonPath("$.data.gallery[0].imageUrl").value("http://example.com/detail1.jpg"))
            .andExpect(jsonPath("$.data.gallery[0].imageOrder").value(1))
            .andExpect(jsonPath("$.data.descriptionHtml").value("<p>상품 상세 설명</p>"))
            .andExpect(jsonPath("$.data.options").isArray())
            .andExpect(jsonPath("$.data.options[0].color").value("블랙"))
            .andExpect(jsonPath("$.data.options[0].colorCode").value("#000000"))
            .andExpect(jsonPath("$.data.options[0].sizes[0].size").value("S"))
            .andExpect(jsonPath("$.data.options[0].sizes[0].stock").value(10))
            .andExpect(jsonPath("$.data.options[0].sizes[0].soldOut").value(false))
            .andExpect(jsonPath("$.data.options[0].sizes[1].soldOut").value(true))
            .andDo(document("products/detail-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 상세 조회")
                    .description("상품 ID로 상세 정보를 조회합니다. 갤러리 이미지, 색상/사이즈 옵션, 할인 정보가 포함됩니다.")
                    .responseSchema(Schema.schema("ProductDetailResponse"))
                    .pathParameters(
                        parameterWithName("productId").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data.categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("data.productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.simpleDescription").type(JsonFieldType.STRING).description("간단 설명"),
                        fieldWithPath("data.mainImageUrl").type(JsonFieldType.STRING).description("메인 이미지 URL"),
                        fieldWithPath("data.originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.sellingPrice").type(JsonFieldType.NUMBER).description("판매가"),
                        fieldWithPath("data.discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)"),
                        fieldWithPath("data.gallery").type(JsonFieldType.ARRAY).description("갤러리 이미지 목록"),
                        fieldWithPath("data.gallery[].imageUrl").type(JsonFieldType.STRING).description("갤러리 이미지 URL"),
                        fieldWithPath("data.gallery[].imageOrder").type(JsonFieldType.NUMBER).description("갤러리 이미지 순서"),
                        fieldWithPath("data.descriptionHtml").type(JsonFieldType.VARIES).optional().description("상품 상세 HTML"),
                        fieldWithPath("data.options").type(JsonFieldType.ARRAY).description("색상 옵션 목록"),
                        fieldWithPath("data.options[].color").type(JsonFieldType.STRING).description("색상명"),
                        fieldWithPath("data.options[].colorCode").type(JsonFieldType.STRING).description("색상 코드"),
                        fieldWithPath("data.options[].sizes").type(JsonFieldType.ARRAY).description("사이즈 옵션 목록"),
                        fieldWithPath("data.options[].sizes[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID (장바구니 담기 시 사용)"),
                        fieldWithPath("data.options[].sizes[].size").type(JsonFieldType.STRING).description("사이즈"),
                        fieldWithPath("data.options[].sizes[].stock").type(JsonFieldType.NUMBER).description("재고 수량"),
                        fieldWithPath("data.options[].sizes[].soldOut").type(JsonFieldType.BOOLEAN).description("품절 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/{productId} - 존재하지 않는 상품 조회 시 404 반환")
    void getProductDetail_notFound() throws Exception {
        // given
        given(productService.getProductDetail(anyLong()))
            .willThrow(new BusinessException(UserErrorCode.PRODUCT_NOT_FOUND));

        // when & then
        mockMvc.perform(get(BASE_URL + "/{productId}", 999L)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."))
            .andDo(document("products/detail-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 상세 조회 - 상품 없음")
                    .description("존재하지 않는 상품 ID로 조회 시 404를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .pathParameters(
                        parameterWithName("productId").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/category/{categoryId} - 해당 카테고리 상품이 없는 경우 빈 목록 반환")
    void getCategoryProductsOnSale_empty() throws Exception {
        // given
        given(productService.getOnSaleProductsByCategory(
            anyLong(), any(ProductSortType.class), any(SortDirection.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 8), 0L));

        // when & then
        mockMvc.perform(get(BASE_URL + "/category/{categoryId}", 999L)
                .param("page", "0")
                .param("size", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andDo(document("products/category-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("카테고리별 상품 전체 페이지 조회 - 빈 목록")
                    .description("해당 카테고리에 상품이 없는 경우 빈 목록을 반환합니다.")
                    .responseSchema(Schema.schema("ProductPageResponse"))
                    .pathParameters(
                        parameterWithName("categoryId").description("카테고리 ID").type(SimpleType.INTEGER)
                    )
                    .queryParameters(
                        parameterWithName("page").description("페이지 번호").type(SimpleType.INTEGER),
                        parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("상품 목록 (빈 배열)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 상품 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/{productId}/options - 상품 옵션 조회 성공")
    void getProductOptions_success() throws Exception {
        // given
        List<ProductColorOption> options = List.of(
            new ProductColorOption("블랙", "#000000", List.of(
                new ProductSizeOption(1L, "S", 10, false),
                new ProductSizeOption(2L, "M", 0, true)
            )),
            new ProductColorOption("화이트", "#FFFFFF", List.of(
                new ProductSizeOption(3L, "S", 5, false),
                new ProductSizeOption(4L, "L", 3, false)
            ))
        );

        given(productService.getProductOptions(anyLong())).willReturn(options);

        // when & then
        mockMvc.perform(get(BASE_URL + "/{productId}/options", 1L)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].color").value("블랙"))
            .andExpect(jsonPath("$.data[0].colorCode").value("#000000"))
            .andExpect(jsonPath("$.data[0].sizes[0].productDetailId").value(1))
            .andExpect(jsonPath("$.data[0].sizes[0].size").value("S"))
            .andExpect(jsonPath("$.data[0].sizes[0].stock").value(10))
            .andExpect(jsonPath("$.data[0].sizes[0].soldOut").value(false))
            .andExpect(jsonPath("$.data[0].sizes[1].productDetailId").value(2))
            .andExpect(jsonPath("$.data[0].sizes[1].soldOut").value(true))
            .andDo(document("products/options-01-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 옵션 조회")
                    .description("상품 ID로 색상/사이즈 옵션 목록을 조회합니다. sizes[].productDetailId는 장바구니 담기/수정 시 사용합니다.")
                    .responseSchema(Schema.schema("ProductOptionsResponse"))
                    .pathParameters(
                        parameterWithName("productId").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("색상 옵션 목록"),
                        fieldWithPath("data[].color").type(JsonFieldType.STRING).description("색상명"),
                        fieldWithPath("data[].colorCode").type(JsonFieldType.STRING).description("색상 코드 (HEX)"),
                        fieldWithPath("data[].sizes").type(JsonFieldType.ARRAY).description("사이즈 옵션 목록"),
                        fieldWithPath("data[].sizes[].productDetailId").type(JsonFieldType.NUMBER).description("상품 상세 ID (장바구니 담기/수정 시 사용)"),
                        fieldWithPath("data[].sizes[].size").type(JsonFieldType.STRING).description("사이즈"),
                        fieldWithPath("data[].sizes[].stock").type(JsonFieldType.NUMBER).description("재고 수량"),
                        fieldWithPath("data[].sizes[].soldOut").type(JsonFieldType.BOOLEAN).description("품절 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/{productId}/options - 옵션이 없는 상품 조회 시 빈 배열 반환")
    void getProductOptions_empty() throws Exception {
        // given
        given(productService.getProductOptions(anyLong())).willReturn(List.of());

        // when & then
        mockMvc.perform(get(BASE_URL + "/{productId}/options", 1L)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andDo(document("products/options-02-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 옵션 조회 - 옵션 없음")
                    .description("등록된 옵션이 없는 상품 조회 시 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("ProductOptionsResponse"))
                    .pathParameters(
                        parameterWithName("productId").description("상품 ID").type(SimpleType.INTEGER)
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("색상 옵션 목록 (빈 배열)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/search - 키워드 검색 성공")
    void searchProducts_success() throws Exception {
        // given
        given(productService.searchProducts(
                any(String.class), any(ProductSortType.class), any(SortDirection.class), any(Pageable.class)))
            .willReturn(createProductPage());

        // when & then
        mockMvc.perform(get(BASE_URL + "/search")
                .param("keyword", "바지")
                .param("sortType", "NEW")
                .param("direction", "DESC")
                .param("page", "0")
                .param("size", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(2))
            .andExpect(jsonPath("$.data.content[0].productName").value("루즈핏 티셔츠"))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andDo(document("products/search-01-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 키워드 검색")
                    .description("상품명에 키워드가 포함된 상품을 검색합니다. (LIKE %keyword% 방식, 대소문자 구분 없음)")
                    .responseSchema(Schema.schema("ProductSearchResponse"))
                    .queryParameters(
                        parameterWithName("keyword").description("검색 키워드 (예: 바지)").type(SimpleType.STRING),
                        parameterWithName("sortType").description("정렬 기준 (NEW, LATEST, NAME, PRICE, POPULAR) - 기본값: NEW").type(SimpleType.STRING).optional(),
                        parameterWithName("direction").description("정렬 방향 (ASC, DESC) - 기본값: DESC").type(SimpleType.STRING).optional(),
                        parameterWithName("page").description("페이지 번호 (0부터 시작)").type(SimpleType.INTEGER).optional(),
                        parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER).optional()
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("검색된 상품 목록"),
                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data.content[].categoryName").type(JsonFieldType.STRING).description("카테고리명"),
                        fieldWithPath("data.content[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.content[].mainImageUrl").type(JsonFieldType.STRING).description("메인 이미지 URL"),
                        fieldWithPath("data.content[].hoverImageUrl").type(JsonFieldType.STRING).optional().description("호버 이미지 URL (없을 수 있음)"),
                        fieldWithPath("data.content[].originalPrice").type(JsonFieldType.NUMBER).description("정가"),
                        fieldWithPath("data.content[].sellingPrice").type(JsonFieldType.NUMBER).description("판매가"),
                        fieldWithPath("data.content[].discountRate").type(JsonFieldType.NUMBER).optional().description("할인율 (없을 시 null)"),
                        fieldWithPath("data.content[].colorCodes").type(JsonFieldType.ARRAY).description("색상 코드 목록 (HEX 문자열 배열, 예: [\"#000000\", \"#FFFFFF\"])"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 검색 결과 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/search - 검색 결과가 없는 경우 빈 목록 반환")
    void searchProducts_empty() throws Exception {
        // given
        given(productService.searchProducts(
                any(String.class), any(ProductSortType.class), any(SortDirection.class), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 8), 0L));

        // when & then
        mockMvc.perform(get(BASE_URL + "/search")
                .param("keyword", "존재하지않는상품명")
                .param("page", "0")
                .param("size", "8")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andDo(document("products/search-02-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 키워드 검색 - 결과 없음")
                    .description("검색 결과가 없는 경우 빈 목록을 반환합니다.")
                    .responseSchema(Schema.schema("ProductSearchResponse"))
                    .queryParameters(
                        parameterWithName("keyword").description("검색 키워드").type(SimpleType.STRING),
                        parameterWithName("page").description("페이지 번호").type(SimpleType.INTEGER).optional(),
                        parameterWithName("size").description("페이지 크기").type(SimpleType.INTEGER).optional()
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("검색된 상품 목록 (빈 배열)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 검색 결과 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/search - 빈 키워드 요청 시 400 반환")
    void searchProducts_blankKeyword() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search")
                .param("keyword", "  ")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @Test
    @DisplayName("GET /v1/products/search - 50자 초과 키워드 요청 시 400 반환")
    void searchProducts_keywordTooLong() throws Exception {
        String longKeyword = "a".repeat(51);

        mockMvc.perform(get(BASE_URL + "/search")
                .param("keyword", longKeyword)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."));
    }

    @Test
    @DisplayName("GET /v1/products/{productId}/reviews - 상품 리뷰 목록 조회 성공")
    void getProductReviews_success() throws Exception {
        com.daebbang.daebbangcore.domain.review.entity.Review review = org.mockito.Mockito.mock(com.daebbang.daebbangcore.domain.review.entity.Review.class);
        com.daebbang.daebbangcore.domain.user.entity.Users user =
            com.daebbang.daebbangcore.domain.user.entity.Users.createLocalUser("testuser123", "encoded", "홍길동", "test@example.com", "01012345678");
        com.daebbang.daebbangcore.domain.review.entity.ReviewImage image =
            org.mockito.Mockito.mock(com.daebbang.daebbangcore.domain.review.entity.ReviewImage.class);

        org.mockito.Mockito.when(review.getId()).thenReturn(1L);
        org.mockito.Mockito.when(review.getUser()).thenReturn(user);
        org.mockito.Mockito.when(review.getRating()).thenReturn(5);
        org.mockito.Mockito.when(review.getContent()).thenReturn("정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다.");
        org.mockito.Mockito.when(review.getCreatedAt()).thenReturn(java.time.LocalDateTime.of(2026, 4, 27, 10, 0));
        org.mockito.Mockito.when(review.getReply()).thenReturn(null);
        org.mockito.Mockito.when(review.getReplyUpdatedAt()).thenReturn(null);
        org.mockito.Mockito.when(image.getImageUrl()).thenReturn("https://s3.example.com/review/image1.jpg");
        org.mockito.Mockito.when(review.getImages()).thenReturn(List.of(image));

        org.springframework.data.domain.Page<com.daebbang.daebbangcore.domain.review.entity.Review> page =
            new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        given(reviewService.getProductReviews(anyLong(), any())).willReturn(page);

        mockMvc.perform(get(BASE_URL + "/10/reviews")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].maskedLoginId").value("test*******"))
            .andExpect(jsonPath("$.data.content[0].rating").value(5))
            .andDo(document("product/product-review-list",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 리뷰 목록 조회")
                    .description("""
                        특정 상품의 리뷰 목록을 조회합니다. 최신순, 한 페이지에 10개.
                        작성자 아이디는 앞 4글자만 표기되고 나머지는 *로 마스킹됩니다.
                        """)
                    .responseSchema(Schema.schema("ProductReviewListResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("리뷰 목록"),
                        fieldWithPath("data.content[].reviewId").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                        fieldWithPath("data.content[].maskedLoginId").type(JsonFieldType.STRING).description("마스킹된 아이디 (앞 4자리 + ****)"),
                        fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("작성 일자"),
                        fieldWithPath("data.content[].rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                        fieldWithPath("data.content[].content").type(JsonFieldType.STRING).description("리뷰 내용 (최소 20자~최대 300자)"),
                        fieldWithPath("data.content[].imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록 (0~4장)"),
                        fieldWithPath("data.content[].reply").type(JsonFieldType.NULL).optional().description("관리자 답글 (없으면 null)"),
                        fieldWithPath("data.content[].replyUpdatedAt").type(JsonFieldType.NULL).optional().description("답글 일자 (없으면 null)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 리뷰 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/products/{productId}/reviews/stats - 리뷰 통계 조회 성공")
    void getProductReviewStats_success() throws Exception {
        com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult stats =
            new com.daebbang.daebbangcore.domain.review.dto.ReviewStatsResult(
                15L,
                4.3,
                java.util.Map.of(1, 0L, 2, 1L, 3, 2L, 4, 5L, 5, 7L)
            );

        given(reviewService.getProductReviewStats(anyLong())).willReturn(stats);

        mockMvc.perform(get(BASE_URL + "/10/reviews/stats")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalCount").value(15))
            .andExpect(jsonPath("$.data.averageRating").value(4.3))
            .andDo(document("product/product-review-stats",
                resource(ResourceSnippetParameters.builder()
                    .tag("Product")
                    .summary("상품 리뷰 통계 조회")
                    .description("상품의 리뷰 통계를 조회합니다. 총 리뷰 수, 평균 별점, 별점별 리뷰 수를 반환합니다.")
                    .responseSchema(Schema.schema("ProductReviewStatsResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.totalCount").type(JsonFieldType.NUMBER).description("총 리뷰 수"),
                        fieldWithPath("data.averageRating").type(JsonFieldType.NUMBER).description("평균 별점 (소수점 1자리)"),
                        fieldWithPath("data.ratingCounts").type(JsonFieldType.OBJECT).description("별점별 리뷰 수"),
                        fieldWithPath("data.ratingCounts.1").type(JsonFieldType.NUMBER).description("1점 리뷰 수"),
                        fieldWithPath("data.ratingCounts.2").type(JsonFieldType.NUMBER).description("2점 리뷰 수"),
                        fieldWithPath("data.ratingCounts.3").type(JsonFieldType.NUMBER).description("3점 리뷰 수"),
                        fieldWithPath("data.ratingCounts.4").type(JsonFieldType.NUMBER).description("4점 리뷰 수"),
                        fieldWithPath("data.ratingCounts.5").type(JsonFieldType.NUMBER).description("5점 리뷰 수")
                    )
                    .build()
                )));
    }
}
