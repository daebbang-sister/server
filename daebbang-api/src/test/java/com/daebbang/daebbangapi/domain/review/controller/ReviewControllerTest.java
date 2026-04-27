package com.daebbang.daebbangapi.domain.review.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointStatus;
import com.daebbang.daebbangcore.domain.review.service.ReviewService;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.lang.reflect.Constructor;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ReviewController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    private static final UsernamePasswordAuthenticationToken AUTH =
        new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    private Review mockReview() {
        Review review = mock(Review.class);
        Users user = Users.createLocalUser("testuser123", "encoded", "홍길동", "test@example.com", "01012345678");
        Products product = mock(Products.class);

        when(review.getId()).thenReturn(1L);
        when(review.getUser()).thenReturn(user);
        when(review.getProduct()).thenReturn(product);
        when(review.getRating()).thenReturn(5);
        when(review.getContent()).thenReturn("정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다.");
        when(review.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 4, 27, 10, 0));
        when(review.getReply()).thenReturn(null);
        when(review.getReplyUpdatedAt()).thenReturn(null);
        when(review.getPointStatus()).thenReturn(ReviewPointStatus.PENDING);
        when(review.getImages()).thenReturn(List.of());
        when(product.getId()).thenReturn(10L);
        when(product.getProductName()).thenReturn("촉촉한 크림빵");

        return review;
    }

    private ReviewPointConfig buildConfig() {
        try {
            Constructor<ReviewPointConfig> ctor = ReviewPointConfig.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ReviewPointConfig config = ctor.newInstance();
            ReflectionTestUtils.setField(config, "normalReviewPoint", 500);
            ReflectionTestUtils.setField(config, "photoReviewPoint", 1000);
            ReflectionTestUtils.setField(config, "autoApproveDays", 7);
            return config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("POST /v1/reviews - 리뷰 작성 성공 (일반 리뷰)")
    void createReview_success_normal() throws Exception {
        willDoNothing().given(reviewService).createReview(any());

        mockMvc.perform(post("/v1/reviews")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderDetailId": 100,
                        "rating": 5,
                        "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
                    }
                    """))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("리뷰가 등록되었습니다."))
            .andDo(document("review/create-review-normal",
                resource(ResourceSnippetParameters.builder()
                    .tag("Review")
                    .summary("리뷰 작성")
                    .description("""
                        주문 완료된 상품에 대해 리뷰를 작성합니다.
                        - 별점: 1~5점 (1점 단위)
                        - 내용: 최소 20자, 최대 300자
                        - 이미지: 최대 4장 (S3 URL)
                        - 이미지 없으면 일반 리뷰, 있으면 포토 리뷰 적립금 적용
                        - 적립금은 초기 대기 상태로 등록됨
                        """)
                    .requestSchema(Schema.schema("CreateReviewRequest"))
                    .responseSchema(Schema.schema("ReviewCreatedResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("orderDetailId").type(JsonFieldType.NUMBER).description("주문 상세 ID"),
                        fieldWithPath("rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                        fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용 (20~300자)"),
                        fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).optional().description("이미지 URL 목록 (최대 4장, 선택)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).optional().description("없음")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/reviews - 리뷰 작성 성공 (포토 리뷰)")
    void createReview_success_photo() throws Exception {
        willDoNothing().given(reviewService).createReview(any());

        mockMvc.perform(post("/v1/reviews")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderDetailId": 101,
                        "rating": 4,
                        "content": "빵이 정말 부드럽고 맛있어요. 다음에도 꼭 주문할 것 같아요!",
                        "imageUrls": ["https://s3.example.com/review/img1.jpg", "https://s3.example.com/review/img2.jpg"]
                    }
                    """))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andDo(document("review/create-review-photo",
                resource(ResourceSnippetParameters.builder()
                    .tag("Review")
                    .summary("리뷰 작성 (포토 리뷰)")
                    .description("이미지를 포함한 포토 리뷰 작성. 이미지가 1장 이상이면 포토 리뷰 적립금이 적용됩니다.")
                    .requestSchema(Schema.schema("CreateReviewRequest"))
                    .responseSchema(Schema.schema("ReviewCreatedResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("orderDetailId").type(JsonFieldType.NUMBER).description("주문 상세 ID"),
                        fieldWithPath("rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                        fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용 (20~300자)"),
                        fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록 (최대 4장)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).optional().description("없음")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/reviews - 리뷰 내용이 20자 미만이면 400 반환")
    void createReview_fail_contentTooShort() throws Exception {
        mockMvc.perform(post("/v1/reviews")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderDetailId": 100,
                        "rating": 5,
                        "content": "짧은 리뷰"
                    }
                    """))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/reviews - 인증 없이 접근 시 401 반환")
    void createReview_unauthorized() throws Exception {
        mockMvc.perform(post("/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "orderDetailId": 100,
                        "rating": 5,
                        "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
                    }
                    """))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /v1/reviews/{reviewId} - 리뷰 수정 성공")
    void updateReview_success() throws Exception {
        willDoNothing().given(reviewService).updateReview(any());

        mockMvc.perform(put("/v1/reviews/1")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "rating": 4,
                        "content": "수정된 리뷰입니다. 두 번째 방문이었는데 여전히 맛있네요."
                    }
                    """))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("리뷰가 수정되었습니다."))
            .andDo(document("review/update-review",
                resource(ResourceSnippetParameters.builder()
                    .tag("Review")
                    .summary("리뷰 수정")
                    .description("작성한 리뷰를 수정합니다. 적립금이 승인된 리뷰는 수정 불가합니다.")
                    .requestSchema(Schema.schema("UpdateReviewRequest"))
                    .responseSchema(Schema.schema("CommonVoidResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                        fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용 (20~300자)"),
                        fieldWithPath("imageUrls").type(JsonFieldType.ARRAY).optional().description("이미지 URL 목록 (최대 4장, 선택)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).optional().description("없음")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("DELETE /v1/reviews/{reviewId} - 리뷰 삭제 성공")
    void deleteReview_success() throws Exception {
        willDoNothing().given(reviewService).deleteReview(anyLong(), anyLong());

        mockMvc.perform(delete("/v1/reviews/1")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("리뷰가 삭제되었습니다."))
            .andDo(document("review/delete-review",
                resource(ResourceSnippetParameters.builder()
                    .tag("Review")
                    .summary("리뷰 삭제")
                    .description("작성한 리뷰를 삭제합니다. 적립금이 승인된 리뷰는 삭제 불가합니다.")
                    .responseSchema(Schema.schema("CommonVoidResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).optional().description("없음")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/reviews/my - 내 리뷰 목록 조회 성공")
    void getMyReviews_success() throws Exception {
        Review review = mockReview();
        Page<Review> page = new PageImpl<>(List.of(review), PageRequest.of(0, 10), 1);

        given(reviewService.getMyReviews(anyLong(), any())).willReturn(page);
        given(reviewService.getPointConfig()).willReturn(buildConfig());

        mockMvc.perform(get("/v1/reviews/my")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].reviewId").value(1))
            .andExpect(jsonPath("$.data.content[0].pointStatus").value("적립금 대기"))
            .andExpect(jsonPath("$.data.content[0].expectedPoint").value(500))
            .andDo(document("review/my-review-list",
                resource(ResourceSnippetParameters.builder()
                    .tag("Review")
                    .summary("내 리뷰 목록 조회")
                    .description("내가 작성한 리뷰 목록을 조회합니다. 최신순, 한 페이지에 10개.")
                    .responseSchema(Schema.schema("MyReviewListResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content").type(JsonFieldType.ARRAY).description("리뷰 목록"),
                        fieldWithPath("data.content[].reviewId").type(JsonFieldType.NUMBER).description("리뷰 ID"),
                        fieldWithPath("data.content[].productId").type(JsonFieldType.NUMBER).description("상품 ID"),
                        fieldWithPath("data.content[].productName").type(JsonFieldType.STRING).description("상품명"),
                        fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("작성 일자"),
                        fieldWithPath("data.content[].rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                        fieldWithPath("data.content[].content").type(JsonFieldType.STRING).description("리뷰 내용"),
                        fieldWithPath("data.content[].imageUrls").type(JsonFieldType.ARRAY).description("이미지 URL 목록"),
                        fieldWithPath("data.content[].reply").type(JsonFieldType.NULL).optional().description("관리자 답글 (없으면 null)"),
                        fieldWithPath("data.content[].replyUpdatedAt").type(JsonFieldType.NULL).optional().description("답글 일자 (없으면 null)"),
                        fieldWithPath("data.content[].pointStatus").type(JsonFieldType.STRING).description("적립금 상태 (적립금 대기 / 적립금 승인)"),
                        fieldWithPath("data.content[].expectedPoint").type(JsonFieldType.NUMBER).description("예상 적립금 (원)"),
                        fieldWithPath("data.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 리뷰 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
                    )
                    .build()
                )));
    }
}
