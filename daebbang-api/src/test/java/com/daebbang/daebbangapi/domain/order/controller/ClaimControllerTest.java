package com.daebbang.daebbangapi.domain.order.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
import com.daebbang.daebbangcommon.error.OrderErrorCode;
import com.daebbang.daebbangcore.domain.order.entity.ClaimImage;
import com.daebbang.daebbangcore.domain.order.entity.ClaimStatus;
import com.daebbang.daebbangcore.domain.order.entity.ClaimType;
import com.daebbang.daebbangcore.domain.order.entity.Claims;
import com.daebbang.daebbangcore.domain.order.entity.ReasonType;
import com.daebbang.daebbangcore.domain.order.service.ClaimService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ClaimController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClaimService claimService;

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
            "data", "data",
            MediaType.APPLICATION_JSON_VALUE,
            body.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile imagePart(String filename) {
        return new MockMultipartFile(
            "images", filename,
            MediaType.IMAGE_JPEG_VALUE,
            ("image-content-" + filename).getBytes(StandardCharsets.UTF_8)
        );
    }

    /** 이미지가 포함된 클레임 fixture (imageUrls 1건) */
    private Claims mockClaimWithImage() {
        Claims claim = mock(Claims.class);
        ClaimImage image = mock(ClaimImage.class);
        when(image.getImageUrl()).thenReturn("https://s3.example.com/claim/20260501-abc.jpg");
        when(image.getImageOrder()).thenReturn(1);

        when(claim.getId()).thenReturn(1L);
        when(claim.getClaimType()).thenReturn(ClaimType.REFUND);
        when(claim.getClaimStatus()).thenReturn(ClaimStatus.REQUESTED);
        when(claim.getReasonType()).thenReturn(ReasonType.DEFECT_OR_DAMAGE);
        when(claim.getReasonDetail()).thenReturn("빵이 눌려서 왔어요.");
        when(claim.getQuantity()).thenReturn(1);
        when(claim.getRefundAmount()).thenReturn(12_000);
        when(claim.getRefundPoint()).thenReturn(0);
        when(claim.getPickupReceiver()).thenReturn("홍길동");
        when(claim.getPickupPhone()).thenReturn("010-1234-5678");
        when(claim.getPickupZipCode()).thenReturn("06123");
        when(claim.getPickupAddress()).thenReturn("서울시 강남구 테헤란로 1");
        when(claim.getPickupDetailAddress()).thenReturn("101동 202호");
        when(claim.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 5, 1, 10, 0));
        when(claim.getImages()).thenReturn(List.of(image));
        return claim;
    }

    /** 이미지 없는 클레임 fixture (imageUrls 빈 배열) */
    private Claims mockClaimNoImage() {
        Claims claim = mock(Claims.class);
        when(claim.getId()).thenReturn(2L);
        when(claim.getClaimType()).thenReturn(ClaimType.EXCHANGE);
        when(claim.getClaimStatus()).thenReturn(ClaimStatus.REQUESTED);
        when(claim.getReasonType()).thenReturn(ReasonType.WRONG_SIZE);
        when(claim.getReasonDetail()).thenReturn(null);
        when(claim.getQuantity()).thenReturn(1);
        when(claim.getRefundAmount()).thenReturn(0);
        when(claim.getRefundPoint()).thenReturn(0);
        when(claim.getPickupReceiver()).thenReturn("홍길동");
        when(claim.getPickupPhone()).thenReturn("010-1234-5678");
        when(claim.getPickupZipCode()).thenReturn("06123");
        when(claim.getPickupAddress()).thenReturn("서울시 강남구 테헤란로 1");
        when(claim.getPickupDetailAddress()).thenReturn(null);
        when(claim.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 5, 1, 10, 0));
        when(claim.getImages()).thenReturn(List.of()); // 이미지 없음
        return claim;
    }

    // 공통 응답 필드 (성공 케이스용)
    private static final org.springframework.restdocs.payload.FieldDescriptor[] CLAIM_RESPONSE_FIELDS = {
        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
        fieldWithPath("data.claimId").type(JsonFieldType.NUMBER).description("클레임 ID"),
        fieldWithPath("data.claimType").type(JsonFieldType.STRING).description("클레임 유형 (REFUND / EXCHANGE)"),
        fieldWithPath("data.claimStatus").type(JsonFieldType.STRING).description("처리 상태 (REQUESTED / COMPLETED / REJECTED)"),
        fieldWithPath("data.reasonType").type(JsonFieldType.STRING).description("사유 코드"),
        fieldWithPath("data.reasonDetail").type(JsonFieldType.STRING).optional().description("상세 사유 (없으면 null)"),
        fieldWithPath("data.quantity").type(JsonFieldType.NUMBER).description("클레임 수량"),
        fieldWithPath("data.refundAmount").type(JsonFieldType.NUMBER).description("환불 금액"),
        fieldWithPath("data.refundPoint").type(JsonFieldType.NUMBER).description("환불 포인트"),
        fieldWithPath("data.pickupReceiver").type(JsonFieldType.STRING).description("수거지 수령인"),
        fieldWithPath("data.pickupPhone").type(JsonFieldType.STRING).description("수거지 연락처"),
        fieldWithPath("data.pickupZipCode").type(JsonFieldType.STRING).description("수거지 우편번호"),
        fieldWithPath("data.pickupAddress").type(JsonFieldType.STRING).description("수거지 주소"),
        fieldWithPath("data.pickupDetailAddress").type(JsonFieldType.STRING).optional().description("수거지 상세주소 (없으면 null)"),
        fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("첨부 이미지 URL 목록"),
        fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("신청 일시")
    };

    // 공통 에러 응답 필드 (data: null 포함)
    private static final org.springframework.restdocs.payload.FieldDescriptor[] ERROR_RESPONSE_FIELDS = {
        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
        fieldWithPath("data").type(JsonFieldType.NULL).optional().description("에러 응답 데이터 (null)")
    };

    // 공통 requestPartFields
    private static final org.springframework.restdocs.payload.FieldDescriptor[] CLAIM_REQUEST_PART_FIELDS = {
        fieldWithPath("claimType").type(JsonFieldType.STRING).description("클레임 유형 (REFUND / EXCHANGE)"),
        fieldWithPath("reasonType").type(JsonFieldType.STRING).description("사유 코드 (필수)"),
        fieldWithPath("reasonDetail").type(JsonFieldType.STRING).optional().description("상세 사유 (선택, 최대 300자)"),
        fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("클레임 수량 (최소 1)")
    };

    // ──────────────────────────────────────────────────────────────────────
    // GET /v1/orders/claim/reason-types
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /v1/orders/claim/reason-types - 사유 목록 조회 성공")
    void getReasonTypes_success() throws Exception {
        mockMvc.perform(get("/v1/orders/claim/reason-types")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("클레임 사유 목록 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(ReasonType.values().length))
            .andExpect(jsonPath("$.data[0].code").exists())
            .andExpect(jsonPath("$.data[0].description").exists())
            .andDo(document("claim/get-reason-types",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("클레임 사유 목록 조회")
                    .description("환불/교환 신청 시 선택 가능한 사유 목록을 조회합니다. 인증 불필요.")
                    .responseSchema(Schema.schema("ReasonTypeListResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("사유 목록"),
                        fieldWithPath("data[].code").type(JsonFieldType.STRING).description("사유 코드 (enum)"),
                        fieldWithPath("data[].description").type(JsonFieldType.STRING).description("사유 한글 설명")
                    )
                    .build()
                )));
    }

    // ──────────────────────────────────────────────────────────────────────
    // POST /v1/orders/{orderDetailId}/claim
    // ──────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 클레임 신청 성공 (이미지 없음) → imageUrls 빈 배열 반환")
    void createClaim_success_noImage() throws Exception {
        Claims claim = mockClaimNoImage(); // 이미지 없는 전용 fixture
        given(claimService.createClaim(any())).willReturn(claim);

        String json = """
            {
                "claimType": "EXCHANGE",
                "reasonType": "WRONG_SIZE",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("환불/교환 신청이 완료되었습니다."))
            .andExpect(jsonPath("$.data.claimId").value(2))
            .andExpect(jsonPath("$.data.claimType").value("EXCHANGE"))
            .andExpect(jsonPath("$.data.claimStatus").value("REQUESTED"))
            .andExpect(jsonPath("$.data.reasonType").value("WRONG_SIZE"))
            .andExpect(jsonPath("$.data.reasonDetail").doesNotExist())   // null → 직렬화 제외 확인
            .andExpect(jsonPath("$.data.imageUrls").isArray())
            .andExpect(jsonPath("$.data.imageUrls.length()").value(0))  // 빈 배열 명시적 검증
            .andDo(document("claim/create-claim-no-image",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("환불/교환 신청")
                    .description("""
                        주문 상세 항목에 대해 환불 또는 교환을 신청합니다. multipart/form-data 형식.
                        - data (application/json): 신청 정보 JSON
                        - images (binary[], 선택, 최대 5장): 첨부 이미지 (jpg/jpeg/png/webp)
                        - 수거지 정보는 주문 당시 배송지에서 자동 복사됩니다.
                        - 환불 금액은 (할인가 × 클레임 수량 / 주문 수량)으로 자동 계산됩니다.
                        """)
                    .pathParameters(
                        parameterWithName("orderDetailId").description("주문 상세 ID")
                    )
                    .requestSchema(Schema.schema("ClaimRequest"))
                    .responseSchema(Schema.schema("ClaimResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(CLAIM_RESPONSE_FIELDS)
                    .build()
                ),
                requestParts(
                    partWithName("data").description("신청 정보 (application/json)")
                ),
                requestPartFields("data", CLAIM_REQUEST_PART_FIELDS)
            ));
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 클레임 신청 성공 (이미지 2장) → imageUrls 배열 항목 타입 검증")
    void createClaim_success_withImages() throws Exception {
        Claims claim = mockClaimWithImage(); // 이미지 포함 fixture
        given(claimService.createClaim(any())).willReturn(claim);

        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "reasonDetail": "빵이 눌려서 왔어요.",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .file(imagePart("img1.jpg"))
                .file(imagePart("img2.jpg"))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.claimId").value(1))
            .andExpect(jsonPath("$.data.refundAmount").value(12_000))
            .andExpect(jsonPath("$.data.imageUrls").isArray())
            .andExpect(jsonPath("$.data.imageUrls.length()").value(1))   // fixture: 이미지 1건
            .andExpect(jsonPath("$.data.imageUrls[0]").value("https://s3.example.com/claim/20260501-abc.jpg"))
            .andDo(document("claim/create-claim-with-images",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("환불/교환 신청 (이미지 첨부)")
                    .description("이미지를 첨부한 환불/교환 신청입니다.")
                    .pathParameters(
                        parameterWithName("orderDetailId").description("주문 상세 ID")
                    )
                    .requestSchema(Schema.schema("ClaimRequest"))
                    .responseSchema(Schema.schema("ClaimResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.claimId").type(JsonFieldType.NUMBER).description("클레임 ID"),
                        fieldWithPath("data.claimType").type(JsonFieldType.STRING).description("클레임 유형"),
                        fieldWithPath("data.claimStatus").type(JsonFieldType.STRING).description("처리 상태"),
                        fieldWithPath("data.reasonType").type(JsonFieldType.STRING).description("사유 코드"),
                        fieldWithPath("data.reasonDetail").type(JsonFieldType.STRING).optional().description("상세 사유"),
                        fieldWithPath("data.quantity").type(JsonFieldType.NUMBER).description("클레임 수량"),
                        fieldWithPath("data.refundAmount").type(JsonFieldType.NUMBER).description("환불 금액"),
                        fieldWithPath("data.refundPoint").type(JsonFieldType.NUMBER).description("환불 포인트"),
                        fieldWithPath("data.pickupReceiver").type(JsonFieldType.STRING).description("수거지 수령인"),
                        fieldWithPath("data.pickupPhone").type(JsonFieldType.STRING).description("수거지 연락처"),
                        fieldWithPath("data.pickupZipCode").type(JsonFieldType.STRING).description("수거지 우편번호"),
                        fieldWithPath("data.pickupAddress").type(JsonFieldType.STRING).description("수거지 주소"),
                        fieldWithPath("data.pickupDetailAddress").type(JsonFieldType.STRING).optional().description("수거지 상세주소"),
                        fieldWithPath("data.imageUrls").type(JsonFieldType.ARRAY).description("첨부 이미지 URL 목록 (S3 문자열 배열). 이미지 없으면 빈 배열."),
                        fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("신청 일시")
                    )
                    .build()
                ),
                requestParts(
                    partWithName("data").description("신청 정보 (application/json)"),
                    partWithName("images").description("첨부 이미지 (선택, 최대 5장, jpg/jpeg/png/webp)")
                ),
                requestPartFields("data", CLAIM_REQUEST_PART_FIELDS)
            ));
    }

    // ── 400 Bad Request (유효성 검사) ───────────────────────────────────────

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - claimType 누락이면 400 반환")
    void createClaim_fail_claimTypeMissing() throws Exception {
        String json = """
            {
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(claimService, never()).createClaim(any());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - reasonType 누락이면 400 반환")
    void createClaim_fail_reasonTypeMissing() throws Exception {
        String json = """
            {
                "claimType": "REFUND",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(claimService, never()).createClaim(any());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - quantity 0이면 400 반환 (경계값)")
    void createClaim_fail_quantityZero() throws Exception {
        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 0
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(claimService, never()).createClaim(any());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - quantity 음수이면 400 반환 (경계값)")
    void createClaim_fail_quantityNegative() throws Exception {
        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": -1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(claimService, never()).createClaim(any());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - reasonDetail 300자는 통과 (경계값)")
    void createClaim_success_reasonDetailMaxLength() throws Exception {
        Claims claim = mockClaimNoImage();
        given(claimService.createClaim(any())).willReturn(claim);

        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "reasonDetail": "%s",
                "quantity": 1
            }
            """.formatted("가".repeat(300));

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - reasonDetail 301자이면 400 반환 (경계값)")
    void createClaim_fail_reasonDetailTooLong() throws Exception {
        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "reasonDetail": "%s",
                "quantity": 1
            }
            """.formatted("가".repeat(301));

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());

        verify(claimService, never()).createClaim(any());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 이미지 6장이면 IMAGE_COUNT_EXCEEDED (400)")
    void createClaim_fail_tooManyImages() throws Exception {
        willThrow(new BusinessException(ImageErrorCode.IMAGE_COUNT_EXCEEDED))
            .given(claimService).createClaim(any());

        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .file(imagePart("img1.jpg"))
                .file(imagePart("img2.jpg"))
                .file(imagePart("img3.jpg"))
                .file(imagePart("img4.jpg"))
                .file(imagePart("img5.jpg"))
                .file(imagePart("img6.jpg"))
                .with(authentication(AUTH))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 수량 초과이면 400 반환")
    void createClaim_fail_quantityExceeded() throws Exception {
        willThrow(new BusinessException(OrderErrorCode.CLAIM_QUANTITY_EXCEEDED))
            .given(claimService).createClaim(any());

        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 999
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("클레임 수량이 주문 수량을 초과합니다."))
            .andDo(document("claim/create-claim-400-quantity-exceeded",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("환불/교환 신청 - 400 수량 초과")
                    .description("클레임 수량이 주문 수량을 초과할 때 반환되는 오류입니다.")
                    .pathParameters(parameterWithName("orderDetailId").description("주문 상세 ID"))
                    .requestSchema(Schema.schema("ClaimRequest"))
                    .requestHeaders(headerWithName("Authorization").description("Bearer JWT 토큰"))
                    .responseFields(ERROR_RESPONSE_FIELDS)
                    .build()
                ),
                requestParts(partWithName("data").description("신청 정보 (application/json)")),
                requestPartFields("data", CLAIM_REQUEST_PART_FIELDS)
            ));
    }

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 신청 불가 상태이면 400 반환")
    void createClaim_fail_notAllowed() throws Exception {
        willThrow(new BusinessException(OrderErrorCode.CLAIM_NOT_ALLOWED))
            .given(claimService).createClaim(any());

        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("환불/교환 신청이 불가한 상태입니다."))
            .andDo(document("claim/create-claim-400-not-allowed",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("환불/교환 신청 - 400 신청 불가 상태")
                    .description("주문 상세가 NORMAL 상태가 아닐 때 반환되는 오류입니다.")
                    .pathParameters(parameterWithName("orderDetailId").description("주문 상세 ID"))
                    .requestSchema(Schema.schema("ClaimRequest"))
                    .requestHeaders(headerWithName("Authorization").description("Bearer JWT 토큰"))
                    .responseFields(ERROR_RESPONSE_FIELDS)
                    .build()
                ),
                requestParts(partWithName("data").description("신청 정보 (application/json)")),
                requestPartFields("data", CLAIM_REQUEST_PART_FIELDS)
            ));
    }

    // ── 401 Unauthorized ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 인증 없이 접근 시 401 반환")
    void createClaim_unauthorized() throws Exception {
        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andDo(document("claim/create-claim-401",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("환불/교환 신청 - 401 인증 필요")
                    .description("Authorization 헤더가 없거나 유효하지 않을 때 반환됩니다. 응답 body 없음.")
                    .pathParameters(parameterWithName("orderDetailId").description("주문 상세 ID"))
                    .requestSchema(Schema.schema("ClaimRequest"))
                    // 401 응답은 body 없음 — responseFields 생략
                    .build()
                ),
                requestParts(partWithName("data").description("신청 정보 (application/json)")),
                requestPartFields("data", CLAIM_REQUEST_PART_FIELDS)
            ));
    }

    // ── 409 Conflict ───────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /v1/orders/{orderDetailId}/claim - 이미 신청 중인 클레임 존재하면 409 반환")
    void createClaim_fail_alreadyExists() throws Exception {
        willThrow(new BusinessException(OrderErrorCode.CLAIM_ALREADY_EXISTS))
            .given(claimService).createClaim(any());

        String json = """
            {
                "claimType": "REFUND",
                "reasonType": "DEFECT_OR_DAMAGE",
                "quantity": 1
            }
            """;

        mockMvc.perform(multipart("/v1/orders/{orderDetailId}/claim", 10L)
                .file(jsonPart(json))
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 처리 중인 환불/교환 신청이 있습니다."))
            .andDo(document("claim/create-claim-409",
                resource(ResourceSnippetParameters.builder()
                    .tag("Claim")
                    .summary("환불/교환 신청 - 409 중복 신청")
                    .description("동일 주문 상세에 처리 중인 클레임이 이미 존재할 때 반환됩니다.")
                    .pathParameters(parameterWithName("orderDetailId").description("주문 상세 ID"))
                    .requestSchema(Schema.schema("ClaimRequest"))
                    .requestHeaders(headerWithName("Authorization").description("Bearer JWT 토큰"))
                    .responseFields(ERROR_RESPONSE_FIELDS)
                    .build()
                ),
                requestParts(partWithName("data").description("신청 정보 (application/json)")),
                requestPartFields("data", CLAIM_REQUEST_PART_FIELDS)
            ));
    }
}
