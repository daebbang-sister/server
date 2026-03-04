package com.daebbang.daebbangapi.domain.users.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.users.dto.request.JoinRequest;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangapi.domain.users.dto.response.UserInfo;
import com.daebbang.daebbangapi.domain.users.dto.vo.AddressVO;
import com.daebbang.daebbangapi.domain.users.mapper.UserMapper;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangapi.provider.TokenProvider;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import java.time.LocalDateTime;
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

@WebMvcTest(controllers = UserController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    @Test
    @DisplayName("GET /v1/users - 회원 정보 조회 성공")
    void getUser_success() throws Exception {
        // given
        String username = "testuser";
        Users mockUser = Users.createLocalUser(username, "encoded_password", "홍길동", "test@example.com", "01012345678");

        UserInfo mockUserInfo = UserInfo.builder()
            .id(1L)
            .provider(Provider.LOCAL)
            .loginId(username)
            .userName("홍길동")
            .userEmail("test@example.com")
            .userPhoneNumber("010-****-5678")
            .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
            .lastLoginAt(null)
            .build();

        given(userService.getUser(username)).willReturn(mockUser);
        given(userMapper.toUserInfo(any(Users.class))).willReturn(mockUserInfo);

        // when & then
        mockMvc.perform(get("/v1/users")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("회원 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.loginId").value(username))
            .andExpect(jsonPath("$.data.userName").value("홍길동"))
            .andExpect(jsonPath("$.data.userEmail").value("test@example.com"))
            .andDo(document("user/get-user",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 정보 조회")
                    .description("JWT 토큰으로 인증된 회원의 정보를 조회합니다.")
                    .responseSchema(Schema.schema("UserInfoResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰 (로그인 후 발급된 액세스 토큰)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원 정보"),
                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("회원 고유 ID"),
                        fieldWithPath("data.provider").type(JsonFieldType.STRING).description("로그인 제공자 (LOCAL, KAKAO)"),
                        fieldWithPath("data.loginId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("data.userName").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("data.userEmail").type(JsonFieldType.STRING).description("이메일 주소"),
                        fieldWithPath("data.userPhoneNumber").type(JsonFieldType.STRING).description("전화번호 (마스킹 처리, 예: 010-****-5678)"),
                        fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("가입 일시"),
                        fieldWithPath("data.lastLoginAt").type(JsonFieldType.VARIES).optional().description("마지막 로그인 일시 (ISO 8601, 미로그인 시 null)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/users - 인증 없이 접근 시 401 반환")
    void getUser_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/users")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/users - 회원 가입 성공")
    void joinUser_success() throws Exception {
        // given
        JoinRequest request = new JoinRequest(
            "홍길동",
            "testuser123",
            "Password123!",
            "010-1234-5678",
            "test@example.com",
            new AddressVO(null,
                "123-4567",
                "테스트시 테스트구 테스트동",
                "테스트 오피스텔 2층")
        );

        willDoNothing().given(userService).join(any(UserJoinCommand.class));

        // when & then
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("회원 가입에 성공하였습니다."))
            .andDo(document("user/join-user",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 가입")
                    .description("새로운 회원을 등록합니다. 전화번호 인증 완료 후 가입이 가능합니다.")
                    .requestSchema(Schema.schema("JoinUserRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("loginId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("전화번호"),
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                        fieldWithPath("address").type(JsonFieldType.OBJECT).optional().description("회원 주소 정보 (null 가능)"),
                        fieldWithPath("address.alias").type(JsonFieldType.VARIES).optional().description("주소 별칭 (null 가능)"),
                        fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address.address").type(JsonFieldType.STRING).description("도로명 주소"),
                        fieldWithPath("address.detailAddress").type(JsonFieldType.STRING).description("상세 주소")
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
    @DisplayName("POST /v1/users - loginId가 영문 소문자/숫자 외 문자 포함 시 400 반환")
    void joinUser_invalidLoginIdPattern() throws Exception {
        // given
        JoinRequest request = new JoinRequest(
            "홍길동",
            "TestUser123",
            "Password123!",
            "010-1234-5678",
            "test@example.com",
            new AddressVO(null,
                "123-4567",
                "테스트시 테스트구 테스트동",
                "테스트 오피스텔 2층")
        );

        // when & then
        mockMvc.perform(post("/v1/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user/join-user-invalid-login-id",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 가입 - loginId 형식 오류")
                    .description("loginId에 영문 소문자/숫자 외 문자(대문자, 특수문자 등)가 포함된 경우 400 Bad Request를 반환합니다.")
                    .requestSchema(Schema.schema("JoinUserRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("loginId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("전화번호"),
                        fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                        fieldWithPath("address").type(JsonFieldType.OBJECT).optional().description("회원 주소 정보 (null 가능)"),
                        fieldWithPath("address.alias").type(JsonFieldType.VARIES).optional().description("주소 별칭 (null 가능)"),
                        fieldWithPath("address.zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address.address").type(JsonFieldType.STRING).description("도로명 주소"),
                        fieldWithPath("address.detailAddress").type(JsonFieldType.STRING).description("상세 주소")
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
    @DisplayName("GET /v1/users/check/id - 아이디 사용 가능 시 200 반환")
    void checkDuplicationId_success() throws Exception {
        // given
        willDoNothing().given(userService).existsActiveUsers(anyString());

        // when & then
        mockMvc.perform(get("/v1/users/check/id")
                .param("loginId", "available123")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("사용 가능한 아이디입니다."))
            .andDo(document("user/check-login-id",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("아이디 중복 확인")
                    .description("입력한 로그인 ID가 이미 사용 중인지 확인합니다. 사용 가능하면 200, 중복이면 409를 반환합니다.")
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .queryParameters(
                        parameterWithName("loginId").description("중복 확인할 로그인 ID (4~16자)")
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
    @DisplayName("GET /v1/users/check/id - 이미 사용 중인 아이디 시 409 반환")
    void checkDuplicationId_duplicateId() throws Exception {
        // given
        willThrow(new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID))
            .given(userService).existsActiveUsers(anyString());

        // when & then
        mockMvc.perform(get("/v1/users/check/id")
                .param("loginId", "takenuser1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다."))
            .andDo(document("user/check-login-id-duplicate",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("아이디 중복 확인 - 중복 오류")
                    .description("이미 사용 중인 로그인 ID인 경우 409 Conflict를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .queryParameters(
                        parameterWithName("loginId").description("중복 확인할 로그인 ID (4~16자)")
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
    @DisplayName("GET /v1/users/check/id - loginId가 빈 값일 때 400 반환")
    void checkDuplicationId_blankLoginId() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/users/check/id")
                .param("loginId", "")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user/check-login-id-blank",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("아이디 중복 확인 - loginId 누락 오류")
                    .description("loginId가 빈 값인 경우 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .queryParameters(
                        parameterWithName("loginId").description("중복 확인할 로그인 ID (4~16자)")
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
    @DisplayName("GET /v1/users/check/id - loginId가 4자 미만일 때 400 반환")
    void checkDuplicationId_invalidLoginId() throws Exception {
        // when & then
        mockMvc.perform(get("/v1/users/check/id")
                .param("loginId", "abc")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user/check-login-id-invalid",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("아이디 중복 확인 - loginId 형식 오류")
                    .description("loginId가 4자 미만이거나 16자 초과인 경우 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .queryParameters(
                        parameterWithName("loginId").description("중복 확인할 로그인 ID (4~16자)")
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
}
