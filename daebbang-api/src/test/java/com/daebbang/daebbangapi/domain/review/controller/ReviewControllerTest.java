package com.daebbang.daebbangapi.domain.review.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.user.service.CustomUserDetailsService;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.ImageErrorCode;
import com.daebbang.daebbangcore.domain.product.entity.Products;
import com.daebbang.daebbangcore.domain.review.entity.Review;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointConfig;
import com.daebbang.daebbangcore.domain.review.entity.ReviewPointStatus;
import com.daebbang.daebbangcore.domain.review.service.ReviewService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

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

    private MockMultipartFile jsonPart(String body) {
        return new MockMultipartFile(
            "data",
            "data",
            MediaType.APPLICATION_JSON_VALUE,
            body.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile imagePart(String filename) {
        return new MockMultipartFile(
            "images",
            filename,
            MediaType.IMAGE_JPEG_VALUE,
            ("image-content-" + filename).getBytes(StandardCharsets.UTF_8)
        );
    }

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
    @DisplayName("POST /v1/reviews - 리뷰 작성 성공 (이미지 없음)")
    void createReview_success_normal() throws Exception {
        willDoNothing().given(reviewService).createReview(any());

        String json = """
            {
                "orderDetailId": 100,
                "rating": 5,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;

        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
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
                        주문 완료된 상품에 대해 리뷰를 작성합니다. multipart/form-data 형식.
<<<<<<< Updated upstream

                        Parts:
                        - data (application/json) — 리뷰 정보 JSON
                          - orderDetailId (number, required): 주문 상세 ID
                          - rating (number, required, 1~5): 별점
                          - content (string, required, 20~300자): 리뷰 내용
                        - images (binary[], optional, 최대 4장) — 첨부 이미지 (jpg, jpeg, png, webp)

=======
                        - data (application/json): 리뷰 정보 JSON
                        - images (binary[], 선택, 최대 4장): 첨부 이미지 (jpg/jpeg/png/webp)
>>>>>>> Stashed changes
                        - 이미지 없으면 일반 리뷰, 1장 이상이면 포토 리뷰 적립금 적용
                        - 적립금은 초기 대기 상태로 등록됨
                        """)
                    .requestSchema(Schema.schema("CreateReviewRequest"))
                    .responseSchema(Schema.schema("ReviewCreatedResponse"))
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
                ),
                requestParts(
                    partWithName("data").description("리뷰 정보 (application/json)")
                ),
                requestPartFields("data",
                    fieldWithPath("orderDetailId").type(JsonFieldType.NUMBER).description("주문 상세 ID"),
                    fieldWithPath("rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용 (20~300자)")
                )));
    }

    @Test
    @DisplayName("POST /v1/reviews - 리뷰 작성 성공 (이미지 2장 업로드)")
    void createReview_success_photo() throws Exception {
        willDoNothing().given(reviewService).createReview(any());

        String json = """
            {
                "orderDetailId": 101,
                "rating": 4,
                "content": "빵이 정말 부드럽고 맛있어요. 다음에도 꼭 주문할 것 같아요!"
            }
            """;

        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .file(imagePart("img1.jpg"))
                .file(imagePart("img2.jpg"))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
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
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).optional().description("없음")
                    )
                    .build()
                ),
                requestParts(
                    partWithName("data").description("리뷰 정보 (application/json)"),
                    partWithName("images").description("첨부 이미지 (선택, 최대 4장, jpg/jpeg/png/webp)")
                ),
                requestPartFields("data",
                    fieldWithPath("orderDetailId").type(JsonFieldType.NUMBER).description("주문 상세 ID"),
                    fieldWithPath("rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용 (20~300자)")
                )));
    }

    @Test
    @DisplayName("POST /v1/reviews - 이미지 5장이면 서비스에서 IMAGE_COUNT_EXCEEDED 발생")
    void createReview_fail_tooManyImages() throws Exception {
        willThrow(new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED))
            .given(reviewService).createReview(any());

        String json = """
            {
                "orderDetailId": 100,
                "rating": 5,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;

        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .file(imagePart("img1.jpg"))
                .file(imagePart("img2.jpg"))
                .file(imagePart("img3.jpg"))
                .file(imagePart("img4.jpg"))
                .file(imagePart("img5.jpg"))
<<<<<<< Updated upstream
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이미지는 최대 4장까지 등록 가능합니다."));
    }

    @Test
    @DisplayName("POST /v1/reviews - 리뷰 내용이 20자 미만이면 400 반환")
    void createReview_fail_contentTooShort() throws Exception {
        String json = """
            {
                "orderDetailId": 100,
                "rating": 5,
                "content": "짧은 리뷰"
            }
            """;

        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
=======
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("이미지는 최대 4장까지 등록 가능합니다."));
    }

    @Test
    @DisplayName("POST /v1/reviews - 리뷰 내용이 20자 미만이면 400 반환")
    void createReview_fail_contentTooShort() throws Exception {
        String json = """
            {
                "orderDetailId": 100,
                "rating": 5,
                "content": "짧은 리뷰"
            }
            """;

        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
>>>>>>> Stashed changes
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(reviewService, never()).createReview(any());
    }

    @Test
    @DisplayName("POST /v1/reviews - 인증 없이 접근 시 401 반환")
    void createReview_unauthorized() throws Exception {
        String json = """
            {
                "orderDetailId": 100,
                "rating": 5,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;

        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/reviews - rating 0 이면 400 반환")
    void createReview_fail_ratingTooLow() throws Exception {
        String json = """
            {
                "orderDetailId": 100,
                "rating": 0,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;
        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/reviews - rating 6 이면 400 반환")
    void createReview_fail_ratingTooHigh() throws Exception {
        String json = """
            {
                "orderDetailId": 100,
                "rating": 6,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;
        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/reviews - rating 누락이면 400 반환")
    void createReview_fail_ratingMissing() throws Exception {
        String json = """
            {
                "orderDetailId": 100,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;
        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/reviews - orderDetailId 누락이면 400 반환")
    void createReview_fail_orderDetailIdMissing() throws Exception {
        String json = """
            {
                "rating": 5,
                "content": "정말 맛있는 빵이에요! 촉촉하고 달달해서 온 가족이 좋아합니다."
            }
            """;
        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/reviews - content 301자면 400 반환")
    void createReview_fail_contentTooLong() throws Exception {
        String longContent = "가".repeat(301);
        String json = """
            {
                "orderDetailId": 100,
                "rating": 5,
                "content": "%s"
            }
            """.formatted(longContent);
        mockMvc.perform(multipart("/v1/reviews")
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /v1/reviews/{reviewId} - 리뷰 수정 성공 (이미지 유지 + 새 이미지 추가)")
    void updateReview_success() throws Exception {
        willDoNothing().given(reviewService).updateReview(any());

        String json = """
            {
                "rating": 4,
                "content": "수정된 리뷰입니다. 두 번째 방문이었는데 여전히 맛있네요.",
                "keepImageUrls": ["https://s3.example.com/review/keep-1.jpg"]
            }
            """;

        mockMvc.perform(multipart("/v1/reviews/{reviewId}", 1L)
                .file(jsonPart(json))
                .file(imagePart("new.jpg"))
                .with(req -> { req.setMethod("PUT"); return req; })
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("리뷰가 수정되었습니다."))
            .andDo(document("review/update-review",
                resource(ResourceSnippetParameters.builder()
                    .tag("Review")
                    .summary("리뷰 수정")
                    .description("""
                        작성한 리뷰를 수정합니다. multipart/form-data 형식.
<<<<<<< Updated upstream

                        Parts:
                        - data (application/json) — 수정 정보 JSON
                          - rating (number, required, 1~5): 별점
                          - content (string, required, 20~300자): 리뷰 내용
                          - keepImageUrls (string[], optional): 유지할 기존 이미지 URL 목록
                        - images (binary[], optional) — 새로 추가할 이미지

                        - keepImageUrls + images 합계는 최대 4장
                        - 기존 이미지 중 keepImageUrls에 없는 항목은 S3에서도 자동 삭제
=======
                        - data (application/json): 수정 정보 JSON
                        - images (binary[], 선택): 새로 추가할 이미지
                        - keepImageUrls + images 합계는 최대 4장
                        - 기존 이미지 중 keepImageUrls에 없는 항목은 S3에서 자동 삭제
>>>>>>> Stashed changes
                        - 적립금이 승인된 리뷰는 수정 불가
                        """)
                    .pathParameters(
                        parameterWithName("reviewId").description("리뷰 ID")
                    )
                    .requestSchema(Schema.schema("UpdateReviewRequest"))
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
                ),
                requestParts(
                    partWithName("data").description("리뷰 수정 정보 (application/json)"),
                    partWithName("images").description("새로 추가할 이미지 (선택)")
                ),
                requestPartFields("data",
                    fieldWithPath("rating").type(JsonFieldType.NUMBER).description("별점 (1~5)"),
                    fieldWithPath("content").type(JsonFieldType.STRING).description("리뷰 내용 (20~300자)"),
                    fieldWithPath("keepImageUrls").type(JsonFieldType.ARRAY).optional().description("유지할 기존 이미지 URL 목록 (선택)")
                )));
    }

    @Test
    @DisplayName("PUT /v1/reviews/{reviewId} - keep + new 합계 5장이면 IMAGE_COUNT_EXCEEDED")
    void updateReview_fail_tooManyImages() throws Exception {
        willThrow(new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED))
            .given(reviewService).updateReview(any());

        String json = """
            {
                "rating": 4,
                "content": "수정된 리뷰입니다. 두 번째 방문이었는데 여전히 맛있네요.",
                "keepImageUrls": ["u1","u2","u3"]
            }
            """;

        mockMvc.perform(multipart("/v1/reviews/{reviewId}", 1L)
                .file(jsonPart(json))
                .file(imagePart("a.jpg"))
                .file(imagePart("b.jpg"))
                .with(req -> { req.setMethod("PUT"); return req; })
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
<<<<<<< Updated upstream
=======
    @DisplayName("PUT /v1/reviews/{reviewId} - keepImageUrls가 기존 리뷰 이미지에 없으면 INVALID_KEEP_IMAGE_URL")
    void updateReview_fail_invalidKeepImageUrl() throws Exception {
        willThrow(new BusinessException(ImageErrorCode.INVALID_KEEP_IMAGE_URL))
            .given(reviewService).updateReview(any());

        String json = """
            {
                "rating": 4,
                "content": "수정된 리뷰입니다. 두 번째 방문이었는데 여전히 맛있네요.",
                "keepImageUrls": ["https://s3.example.com/review/not-mine.jpg"]
            }
            """;

        mockMvc.perform(multipart("/v1/reviews/{reviewId}", 1L)
                .file(jsonPart(json))
                .with(req -> { req.setMethod("PUT"); return req; })
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("유지할 수 없는 이미지가 포함되어 있습니다."));
    }

    @Test
>>>>>>> Stashed changes
    @DisplayName("PUT /v1/reviews/{reviewId} - 인증 없이 접근 시 401 반환")
    void updateReview_unauthorized() throws Exception {
        String json = """
            {
                "rating": 4,
                "content": "수정된 리뷰입니다. 두 번째 방문이었는데 여전히 맛있네요."
            }
            """;
        mockMvc.perform(multipart("/v1/reviews/1")
                .file(jsonPart(json))
                .with(req -> { req.setMethod("PUT"); return req; })
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /v1/reviews/{reviewId} - 리뷰 삭제 성공")
    void deleteReview_success() throws Exception {
        willDoNothing().given(reviewService).deleteReview(anyLong(), anyLong());

        mockMvc.perform(delete("/v1/reviews/{reviewId}", 1L)
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
                    .description("작성한 리뷰를 삭제합니다. 등록된 이미지는 S3에서도 함께 삭제됩니다. 적립금이 승인된 리뷰는 삭제 불가합니다.")
                    .pathParameters(
                        parameterWithName("reviewId").description("리뷰 ID")
                    )
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
    @DisplayName("DELETE /v1/reviews/{reviewId} - 인증 없이 접근 시 401 반환")
    void deleteReview_unauthorized() throws Exception {
        mockMvc.perform(delete("/v1/reviews/1"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
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

    @Test
    @DisplayName("GET /v1/reviews/my - 인증 없이 접근 시 401 반환")
    void getMyReviews_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/reviews/my"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /v1/reviews/my - 리뷰 없을 때 빈 페이지 반환")
    void getMyReviews_empty() throws Exception {
        given(reviewService.getMyReviews(anyLong(), any()))
            .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
        given(reviewService.getPointConfig()).willReturn(buildConfig());

        mockMvc.perform(get("/v1/reviews/my")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(0))
            .andExpect(jsonPath("$.data.totalElements").value(0));
    }
}
