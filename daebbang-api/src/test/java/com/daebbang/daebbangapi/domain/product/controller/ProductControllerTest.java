package com.daebbang.daebbangapi.domain.product.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
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
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        given(productService.getNewProductsOnSale(anyInt()))
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
                        parameterWithName("limit").description("조회할 상품 수")
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
        given(productService.getNewProductsOnSale(anyInt()))
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
                        parameterWithName("limit").description("조회할 상품 수")
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
        given(productService.getCategoryProductsOnSale(anyLong(), anyInt()))
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
                        parameterWithName("categoryId").description("카테고리 ID")
                    )
                    .description("메인 페이지 카테고리별 상품 목록을 조회합니다. 등록일 기준 최신순으로 반환됩니다.")
                    .responseSchema(Schema.schema("ProductMainCardListResponse"))
                    .queryParameters(
                        parameterWithName("limit").description("조회할 상품 수")
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
    @DisplayName("GET /v1/products/main/category/{categoryId} - 해당 카테고리 상품이 없는 경우 빈 배열 반환")
    void getMainCategoryProductsOnSale_empty() throws Exception {
        // given
        given(productService.getCategoryProductsOnSale(anyLong(), anyInt()))
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
                        parameterWithName("limit").description("조회할 상품 수")
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
}
