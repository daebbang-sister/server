package com.daebbang.daebbangapi.domain.point.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.user.service.CustomUserDetailsService;
import com.daebbang.daebbangcore.domain.point.dto.PointBalanceResult;
import com.daebbang.daebbangcore.domain.point.entity.AmountType;
import com.daebbang.daebbangcore.domain.point.entity.ChangeType;
import com.daebbang.daebbangcore.domain.point.entity.PointPolicy;
import com.daebbang.daebbangcore.domain.point.entity.PolicyType;
import com.daebbang.daebbangcore.domain.point.entity.Points;
import com.daebbang.daebbangcore.domain.point.entity.UserPointHistory;
import com.daebbang.daebbangcore.domain.point.service.PointService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.epages.restdocs.apispec.SimpleType;
import java.math.BigDecimal;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PointController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointService pointService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    private static final UsernamePasswordAuthenticationToken AUTH =
        new UsernamePasswordAuthenticationToken(1L, null,
            List.of(new SimpleGrantedAuthority("ROLE_USER")));

    // ===== GET /v1/points/me =====

    @Test
    @DisplayName("GET /v1/points/me - 적립금 잔액 조회 성공")
    void getMyBalance_success() throws Exception {
        given(pointService.getBalance(anyLong()))
            .willReturn(new PointBalanceResult(3_500, 12_000, 8_500));

        mockMvc.perform(get("/v1/points/me")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("적립금 잔액 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.currentAmount").value(3_500))
            .andExpect(jsonPath("$.data.totalEarned").value(12_000))
            .andExpect(jsonPath("$.data.totalUsed").value(8_500))
            .andDo(document("point/get-balance",
                resource(ResourceSnippetParameters.builder()
                    .tag("Point")
                    .summary("내 적립금 잔액 조회")
                    .description("로그인한 회원의 사용 가능한 적립금, 누적 적립금, 누적 사용 적립금을 조회합니다.")
                    .responseSchema(Schema.schema("PointBalanceResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.currentAmount").type(JsonFieldType.NUMBER).description("사용 가능한 적립금 (보유액)"),
                        fieldWithPath("data.totalEarned").type(JsonFieldType.NUMBER).description("총 적립금 (누적 적립액)"),
                        fieldWithPath("data.totalUsed").type(JsonFieldType.NUMBER).description("사용된 적립금 (누적 사용액)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/points/me - 적립금 정보 없는 회원도 0으로 조회 성공")
    void getMyBalance_noPointsRecord() throws Exception {
        given(pointService.getBalance(anyLong())).willReturn(PointBalanceResult.zero());

        mockMvc.perform(get("/v1/points/me")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.currentAmount").value(0))
            .andExpect(jsonPath("$.data.totalEarned").value(0))
            .andExpect(jsonPath("$.data.totalUsed").value(0))
            .andDo(document("point/get-balance-zero",
                resource(ResourceSnippetParameters.builder()
                    .tag("Point")
                    .summary("내 적립금 잔액 조회 - 적립금 내역이 없는 회원")
                    .description("적립·사용 이력이 한 번도 없는 회원의 잔액 조회 시 모든 항목 0으로 응답합니다.")
                    .responseSchema(Schema.schema("PointBalanceResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.currentAmount").type(JsonFieldType.NUMBER).description("사용 가능한 적립금 (0)"),
                        fieldWithPath("data.totalEarned").type(JsonFieldType.NUMBER).description("총 적립금 (0)"),
                        fieldWithPath("data.totalUsed").type(JsonFieldType.NUMBER).description("사용된 적립금 (0)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/points/me - 인증 없이 접근 시 401 반환")
    void getMyBalance_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/points/me")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ===== GET /v1/points/me/history =====

    @Test
    @DisplayName("GET /v1/points/me/history - 적립금 내역 조회 성공 (적립/사용 혼합)")
    void getMyHistory_success() throws Exception {
        Users user = Users.createLocalUser("testuser", "encoded", "홍길동",
            "test@example.com", "010-1234-5678");
        Points points = Points.create(user);

        PointPolicy signupPolicy = PointPolicy.create(
            PolicyType.SIGNUP, AmountType.FIXED, BigDecimal.valueOf(1000),
            365, "신규 회원 가입 적립", true);
        ReflectionTestUtils.setField(signupPolicy, "id", 1L);

        UserPointHistory earnHistory = UserPointHistory.ofEarn(
            points, signupPolicy, ChangeType.EARN_SIGNUP, null,
            1000, 1000, "회원가입 적립",
            LocalDateTime.of(2027, 5, 1, 10, 0));
        ReflectionTestUtils.setField(earnHistory, "id", 100L);
        ReflectionTestUtils.setField(earnHistory, "createdAt",
            LocalDateTime.of(2026, 5, 1, 10, 0));

        UserPointHistory useHistory = UserPointHistory.ofChange(
            points, ChangeType.USE_PAYMENT, 5001L,
            500, 500, "결제 시 적립금 사용");
        ReflectionTestUtils.setField(useHistory, "id", 101L);
        ReflectionTestUtils.setField(useHistory, "createdAt",
            LocalDateTime.of(2026, 5, 2, 11, 0));

        Page<UserPointHistory> page = new PageImpl<>(
            List.of(useHistory, earnHistory),
            PageRequest.of(0, 10), 2);

        given(pointService.getHistory(anyLong(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/v1/points/me/history")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("적립금 내역 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.content[0].changeType").value("USE_PAYMENT"))
            .andExpect(jsonPath("$.data.content[0].earn").value(false))
            .andExpect(jsonPath("$.data.content[0].changeAmount").value(500))
            .andExpect(jsonPath("$.data.content[0].referenceId").value(5001))
            .andExpect(jsonPath("$.data.content[1].changeType").value("EARN_SIGNUP"))
            .andExpect(jsonPath("$.data.content[1].earn").value(true))
            .andExpect(jsonPath("$.data.content[1].policyName").value("신규 회원 가입 적립"))
            .andExpect(jsonPath("$.data.content[1].changeAmount").value(1000))
            .andExpect(jsonPath("$.data.totalElements").value(2))
            .andDo(document("point/get-history",
                resource(ResourceSnippetParameters.builder()
                    .tag("Point")
                    .summary("내 적립금 내역 조회")
                    .description("""
                        로그인한 회원의 적립금 변동 내역을 페이징하여 반환합니다.
                        최신 순(생성일 내림차순)으로 정렬됩니다.
                        - 적립 건(EARN_*)에는 policyName과 expiredAt이 포함됩니다.
                        - 사용·환불·만료 건에서는 policyName, expiredAt이 null입니다.
                        - referenceId는 주문 ID, 리뷰 ID 등을 가리키며 회원가입 적립 등에서는 null입니다.
                        """)
                    .responseSchema(Schema.schema("PointHistoryListResponse"))
                    .queryParameters(
                        parameterWithName("page").optional().description("페이지 번호 (0부터, 기본 0)").type(SimpleType.INTEGER),
                        parameterWithName("size").optional().description("페이지 크기 (기본 10)").type(SimpleType.INTEGER),
                        parameterWithName("sort").optional().description("정렬 (기본: createdAt,desc)").type(SimpleType.STRING)
                    )
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("적립금 내역 목록"),
                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("내역 ID"),
                        fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("발생 일시"),
                        fieldWithPath("data.content[].changeType").type(JsonFieldType.STRING).description("변동 타입 (EARN_SIGNUP / EARN_REVIEW / EARN_PURCHASE / USE_PAYMENT / REFUND_CANCEL / REFUND_REVERSE / EXPIRE)"),
                        fieldWithPath("data.content[].changeTypeDescription").type(JsonFieldType.STRING).description("변동 타입 한글 설명"),
                        fieldWithPath("data.content[].earn").type(JsonFieldType.BOOLEAN).description("적립 여부 (true=적립, false=사용/회수/만료)"),
                        fieldWithPath("data.content[].policyName").type(JsonFieldType.STRING).optional().description("적립금 정책명 (적립 건만)"),
                        fieldWithPath("data.content[].referenceId").type(JsonFieldType.NUMBER).optional().description("연결된 외부 ID (주문 ID, 리뷰 ID 등). 없으면 null"),
                        fieldWithPath("data.content[].changeAmount").type(JsonFieldType.NUMBER).description("변동 금액 (양수)"),
                        fieldWithPath("data.content[].pointAmount").type(JsonFieldType.NUMBER).description("변동 후 잔액 스냅샷"),
                        fieldWithPath("data.content[].description").type(JsonFieldType.STRING).description("내역 설명"),
                        fieldWithPath("data.content[].expiredAt").type(JsonFieldType.STRING).optional().description("적립금 소멸 예정 일시 (적립 건만, 무기한이면 null)"),
                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("페이지 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER).description("오프셋"),
                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이징 적용 여부"),
                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("페이징 미적용 여부"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 내역 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 항목 수"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("결과 없음 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/points/me/history - 내역이 없을 때 빈 결과 반환")
    void getMyHistory_empty() throws Exception {
        given(pointService.getHistory(anyLong(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/v1/points/me/history")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isEmpty())
            .andExpect(jsonPath("$.data.totalElements").value(0))
            .andDo(document("point/get-history-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Point")
                    .summary("내 적립금 내역 조회 - 빈 목록")
                    .description("적립·사용 이력이 없는 회원은 빈 페이지로 응답합니다.")
                    .responseSchema(Schema.schema("PointHistoryListResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("적립금 내역 목록 (빈 배열)"),
                        fieldWithPath("data.pageable").type(JsonFieldType.OBJECT).description("페이지 정보"),
                        fieldWithPath("data.pageable.pageNumber").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.pageable.pageSize").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.pageable.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.pageable.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.pageable.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.pageable.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.pageable.offset").type(JsonFieldType.NUMBER).description("오프셋"),
                        fieldWithPath("data.pageable.paged").type(JsonFieldType.BOOLEAN).description("페이징 적용 여부"),
                        fieldWithPath("data.pageable.unpaged").type(JsonFieldType.BOOLEAN).description("페이징 미적용 여부"),
                        fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("전체 내역 수"),
                        fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                        fieldWithPath("data.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                        fieldWithPath("data.first").type(JsonFieldType.BOOLEAN).description("첫 번째 페이지 여부"),
                        fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                        fieldWithPath("data.number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                        fieldWithPath("data.sort").type(JsonFieldType.OBJECT).description("정렬 정보"),
                        fieldWithPath("data.sort.sorted").type(JsonFieldType.BOOLEAN).description("정렬 적용 여부"),
                        fieldWithPath("data.sort.unsorted").type(JsonFieldType.BOOLEAN).description("정렬 미적용 여부"),
                        fieldWithPath("data.sort.empty").type(JsonFieldType.BOOLEAN).description("정렬 조건 없음 여부"),
                        fieldWithPath("data.numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 항목 수"),
                        fieldWithPath("data.empty").type(JsonFieldType.BOOLEAN).description("결과 없음 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/points/me/history - 인증 없이 접근 시 401 반환")
    void getMyHistory_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/points/me/history")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
