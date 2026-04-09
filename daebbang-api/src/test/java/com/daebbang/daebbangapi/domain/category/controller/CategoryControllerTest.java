package com.daebbang.daebbangapi.domain.category.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangcore.domain.category.entity.Category;
import com.daebbang.daebbangcore.domain.category.service.CategoryService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private static final String BASE_URL = "/v1/categories";

    private Category mockSuperCategory(Long id, String name) {
        Category category = mock(Category.class);
        given(category.getId()).willReturn(id);
        given(category.getCategoryName()).willReturn(name);
        given(category.getSuperCategory()).willReturn(null);
        return category;
    }

    private Category mockChildCategory(Category parent, Long id, String name) {
        Category category = mock(Category.class);
        given(category.getId()).willReturn(id);
        given(category.getCategoryName()).willReturn(name);
        given(category.getSuperCategory()).willReturn(parent);
        return category;
    }

    @Test
    @DisplayName("GET /v1/categories - 전체 카테고리 계층 구조 조회 성공")
    void getCategories_success() throws Exception {
        // given
        Category 상의 = mockSuperCategory(1L, "상의");
        Category 하의 = mockSuperCategory(2L, "하의");

        Category 반팔 = mockChildCategory(상의, 3L, "반팔");
        Category 긴팔 = mockChildCategory(상의, 4L, "긴팔");
        Category 청바지 = mockChildCategory(하의, 5L, "청바지");

        given(categoryService.getAllCategories()).willReturn(List.of(상의, 하의, 반팔, 긴팔, 청바지));

        // when & then
        mockMvc.perform(get(BASE_URL)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].categoryName").value("상의"))
            .andExpect(jsonPath("$.data[0].children").isArray())
            .andExpect(jsonPath("$.data[0].children.length()").value(2))
            .andExpect(jsonPath("$.data[0].children[0].categoryName").value("반팔"))
            .andExpect(jsonPath("$.data[0].children[1].categoryName").value("긴팔"))
            .andExpect(jsonPath("$.data[1].categoryName").value("하의"))
            .andExpect(jsonPath("$.data[1].children.length()").value(1))
            .andExpect(jsonPath("$.data[1].children[0].categoryName").value("청바지"))
            .andDo(document("categories/list-success",
                resource(ResourceSnippetParameters.builder()
                    .tag("Category")
                    .summary("전체 카테고리 계층 조회")
                    .description("상위 카테고리와 하위 카테고리를 계층 구조로 반환합니다. 단일 API 호출로 전체 카테고리 트리를 반환합니다.")
                    .responseSchema(Schema.schema("CategoryListResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("상위 카테고리 목록"),
                        fieldWithPath("data[].id").type(JsonFieldType.VARIES).description("상위 카테고리 ID"),
                        fieldWithPath("data[].categoryName").type(JsonFieldType.STRING).description("상위 카테고리명"),
                        fieldWithPath("data[].children").type(JsonFieldType.ARRAY).description("하위 카테고리 목록"),
                        fieldWithPath("data[].children[].id").type(JsonFieldType.VARIES).description("하위 카테고리 ID"),
                        fieldWithPath("data[].children[].categoryName").type(JsonFieldType.STRING).description("하위 카테고리명"),
                        fieldWithPath("data[].children[].children").type(JsonFieldType.ARRAY).description("하위의 하위 카테고리 (현재 미사용, 빈 배열)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/categories - 등록된 카테고리가 없는 경우 빈 목록 반환")
    void getCategories_empty() throws Exception {
        // given
        given(categoryService.getAllCategories()).willReturn(List.of());

        // when & then
        mockMvc.perform(get(BASE_URL)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data").isEmpty())
            .andDo(document("categories/list-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Category")
                    .summary("전체 카테고리 계층 조회 - 빈 목록")
                    .description("등록된 카테고리가 없는 경우 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("CategoryListResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("카테고리 목록 (빈 배열)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/categories - 하위 카테고리가 없는 상위 카테고리는 children 빈 배열로 반환")
    void getCategories_superCategoryWithNoChildren() throws Exception {
        // given
        Category 상의 = mockSuperCategory(1L, "상의");

        given(categoryService.getAllCategories()).willReturn(List.of(상의));

        // when & then
        mockMvc.perform(get(BASE_URL)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].categoryName").value("상의"))
            .andExpect(jsonPath("$.data[0].children").isArray())
            .andExpect(jsonPath("$.data[0].children").isEmpty())
            .andDo(document("categories/list-no-children",
                resource(ResourceSnippetParameters.builder()
                    .tag("Category")
                    .summary("전체 카테고리 계층 조회 - 하위 카테고리 없음")
                    .description("하위 카테고리가 없는 상위 카테고리는 children을 빈 배열로 반환합니다.")
                    .responseSchema(Schema.schema("CategoryListResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("상위 카테고리 목록"),
                        fieldWithPath("data[].id").type(JsonFieldType.VARIES).description("상위 카테고리 ID"),
                        fieldWithPath("data[].categoryName").type(JsonFieldType.STRING).description("상위 카테고리명"),
                        fieldWithPath("data[].children").type(JsonFieldType.ARRAY).description("하위 카테고리 목록 (빈 배열)")
                    )
                    .build()
                )));
    }
}
