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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangcore.domain.product.dto.ProductCardQueryResult;
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

    private static final String BASE_URL = "/v1/products";

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
                ProductStatus.SALE
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
                ProductStatus.SALE
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
            .andExpect(jsonPath("$.data[1].id").value(2L))
            .andExpect(jsonPath("$.data[1].sellingPrice").value(39000))
            .andExpect(jsonPath("$.data[1].discountRate").doesNotExist())
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
                        fieldWithPath("data[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)")
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
                        fieldWithPath("data[].discountRate").type(JsonFieldType.VARIES).optional().description("할인율 % (할인 없을 시 null)")
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
            .andExpect(jsonPath("$.data.content[1].sellingPrice").value(39000))
            .andExpect(jsonPath("$.data.content[1].discountRate").doesNotExist())
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
            .andExpect(jsonPath("$.data.content[1].sellingPrice").value(39000))
            .andExpect(jsonPath("$.data.content[1].discountRate").doesNotExist())
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
}
