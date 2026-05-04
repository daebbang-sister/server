package com.daebbang.daebbangapi.domain.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.user.dto.request.JoinRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.MyInfoUpdateRequest;
import com.daebbang.daebbangapi.domain.user.dto.vo.AddressVO;
import com.daebbang.daebbangapi.domain.user.service.CustomUserDetailsService;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.SmsErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.command.MyInfoUpdateCommand;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import java.util.List;
import java.util.Optional;
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
    private AddressService addressService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    private static final UsernamePasswordAuthenticationToken AUTH =
        new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    @Test
    @DisplayName("GET /v1/users - 기본 주소 없이 회원 정보 조회 성공")
    void getUser_success_without_default_address() throws Exception {
        Users mockUser = Users.createLocalUser("testuser", "encoded_password", "홍길동", "test@example.com", "010-1234-5678");

        given(userService.getUserById(anyLong())).willReturn(mockUser);
        given(addressService.findDefaultByUserId(anyLong())).willReturn(Optional.empty());

        mockMvc.perform(get("/v1/users")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("회원 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.loginId").value("testuser"))
            .andExpect(jsonPath("$.data.userName").value("홍길동"))
            .andExpect(jsonPath("$.data.userEmail").value("test@example.com"))
            .andExpect(jsonPath("$.data.defaultAddress").isEmpty())
            .andDo(document("user/get-user-no-address",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 정보 조회 (기본 주소 없음)")
                    .description("JWT 토큰으로 인증된 회원의 정보를 조회합니다. 기본 배송지가 없을 경우 defaultAddress는 null입니다.")
                    .responseSchema(Schema.schema("UserInfoResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원 정보"),
                        fieldWithPath("data.id").type(JsonFieldType.VARIES).optional().description("회원 고유 ID"),
                        fieldWithPath("data.provider").type(JsonFieldType.STRING).description("로그인 제공자 (LOCAL, KAKAO)"),
                        fieldWithPath("data.loginId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("data.userName").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("data.userEmail").type(JsonFieldType.STRING).description("이메일 주소"),
                        fieldWithPath("data.userPhoneNumber").type(JsonFieldType.STRING).description("전화번호 (마스킹 처리, 예: 010-****-5678)"),
                        fieldWithPath("data.createdAt").type(JsonFieldType.VARIES).optional().description("가입 일시"),
                        fieldWithPath("data.lastLoginAt").type(JsonFieldType.VARIES).optional().description("마지막 로그인 일시 (미로그인 시 null)"),
                        fieldWithPath("data.defaultAddress").type(JsonFieldType.VARIES).optional().description("기본 배송지 (없을 경우 null)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/users - 기본 주소 포함 회원 정보 조회 성공")
    void getUser_success_with_default_address() throws Exception {
        Users mockUser = Users.createLocalUser("testuser", "encoded_password", "홍길동", "test@example.com", "010-1234-5678");
        Address mockAddress = Address.create(
            mockUser, "홍길동", "010-1234-5678", "집",
            com.daebbang.daebbangcore.domain.address.entity.AddressVO.of("12345", "서울시 강남구 테헤란로", "101호"),
            true
        );

        given(userService.getUserById(anyLong())).willReturn(mockUser);
        given(addressService.findDefaultByUserId(anyLong())).willReturn(Optional.of(mockAddress));

        mockMvc.perform(get("/v1/users")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loginId").value("testuser"))
            .andExpect(jsonPath("$.data.defaultAddress.alias").value("집"))
            .andExpect(jsonPath("$.data.defaultAddress.receiver").value("홍길동"))
            .andExpect(jsonPath("$.data.defaultAddress.zipCode").value("12345"))
            .andExpect(jsonPath("$.data.defaultAddress.address").value("서울시 강남구 테헤란로"))
            .andExpect(jsonPath("$.data.defaultAddress.detailAddress").value("101호"))
            .andDo(document("user/get-user-with-address",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 정보 조회 (기본 주소 포함)")
                    .description("기본 배송지가 있을 경우 defaultAddress 필드에 주소 정보가 포함됩니다.")
                    .responseSchema(Schema.schema("UserInfoResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원 정보"),
                        fieldWithPath("data.id").type(JsonFieldType.VARIES).optional().description("회원 고유 ID"),
                        fieldWithPath("data.provider").type(JsonFieldType.STRING).description("로그인 제공자 (LOCAL, KAKAO)"),
                        fieldWithPath("data.loginId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("data.userName").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("data.userEmail").type(JsonFieldType.STRING).description("이메일 주소"),
                        fieldWithPath("data.userPhoneNumber").type(JsonFieldType.STRING).description("전화번호 (마스킹 처리, 예: 010-****-5678)"),
                        fieldWithPath("data.createdAt").type(JsonFieldType.VARIES).optional().description("가입 일시"),
                        fieldWithPath("data.lastLoginAt").type(JsonFieldType.VARIES).optional().description("마지막 로그인 일시"),
                        fieldWithPath("data.defaultAddress").type(JsonFieldType.OBJECT).description("기본 배송지"),
                        fieldWithPath("data.defaultAddress.addressId").type(JsonFieldType.VARIES).optional().description("주소 ID"),
                        fieldWithPath("data.defaultAddress.alias").type(JsonFieldType.VARIES).optional().description("주소 별칭"),
                        fieldWithPath("data.defaultAddress.receiver").type(JsonFieldType.STRING).description("수령인"),
                        fieldWithPath("data.defaultAddress.receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 전화번호"),
                        fieldWithPath("data.defaultAddress.zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("data.defaultAddress.address").type(JsonFieldType.STRING).description("도로명 주소"),
                        fieldWithPath("data.defaultAddress.detailAddress").type(JsonFieldType.STRING).description("상세 주소")
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
    @DisplayName("DELETE /v1/users - 회원 탈퇴 성공")
    void withdrawUser_success() throws Exception {
        willDoNothing().given(userService).withdraw(anyLong());

        mockMvc.perform(delete("/v1/users")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("회원 탈퇴에 성공하였습니다."))
            .andDo(document("user/withdraw-user",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("회원 탈퇴")
                    .description("인증된 회원을 탈퇴 처리합니다. 탈퇴 시 모든 주소록도 함께 삭제됩니다.")
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
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
    @DisplayName("DELETE /v1/users - 인증 없이 접근 시 401 반환")
    void withdrawUser_unauthorized() throws Exception {
        mockMvc.perform(delete("/v1/users")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /v1/users - 회원 가입 성공")
    void joinUser_success() throws Exception {
        JoinRequest request = new JoinRequest(
            "홍길동",
            "testuser123",
            "Password123!",
            "010-1234-5678",
            "test@example.com",
            new AddressVO(null, "123-4567", "테스트시 테스트구 테스트동", "테스트 오피스텔 2층")
        );

        willDoNothing().given(userService).join(any(UserJoinCommand.class));

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
        JoinRequest request = new JoinRequest(
            "홍길동",
            "TestUser123",
            "Password123!",
            "010-1234-5678",
            "test@example.com",
            new AddressVO(null, "123-4567", "테스트시 테스트구 테스트동", "테스트 오피스텔 2층")
        );

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
                    .description("loginId에 영문 소문자/숫자 외 문자가 포함된 경우 400 Bad Request를 반환합니다.")
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
        willDoNothing().given(userService).existsActiveUsers(anyString());

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
        willThrow(new BusinessException(UserErrorCode.DUPLICATE_LOGIN_ID))
            .given(userService).existsActiveUsers(anyString());

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
    @DisplayName("GET /v1/users/me/edit - 마이페이지 회원정보 조회 성공 (LOCAL)")
    void getMyInfoForEdit_success_local() throws Exception {
        Users mockUser = Users.createLocalUser("testuser", "encoded", "홍길동", "test@example.com", "010-1234-5678");

        given(userService.getUserById(anyLong())).willReturn(mockUser);

        mockMvc.perform(get("/v1/users/me/edit")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.provider").value("LOCAL"))
            .andExpect(jsonPath("$.data.loginId").value("testuser"))
            .andExpect(jsonPath("$.data.userName").value("홍길동"))
            .andExpect(jsonPath("$.data.userEmail").value("test@example.com"))
            .andExpect(jsonPath("$.data.userPhoneNumber").value("010-1234-5678"))
            .andDo(document("user/get-my-info-edit-local",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("마이페이지 회원정보 조회 (수정용)")
                    .description("마이페이지 수정 화면에서 사용할 본인 정보를 조회합니다. 마스킹되지 않은 원본 전화번호와 provider를 함께 내려주어 프론트가 비밀번호 변경 가능 여부를 판단할 수 있습니다.")
                    .responseSchema(Schema.schema("MyInfoEditResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("회원 수정용 정보"),
                        fieldWithPath("data.id").type(JsonFieldType.VARIES).optional().description("회원 고유 ID"),
                        fieldWithPath("data.provider").type(JsonFieldType.STRING).description("로그인 제공자 (LOCAL: 비밀번호 변경 가능, KAKAO: 비밀번호 변경 불가)"),
                        fieldWithPath("data.loginId").type(JsonFieldType.STRING).description("로그인 ID (변경 불가)"),
                        fieldWithPath("data.userName").type(JsonFieldType.STRING).description("회원 이름 (변경 불가)"),
                        fieldWithPath("data.userEmail").type(JsonFieldType.STRING).description("이메일 주소"),
                        fieldWithPath("data.userPhoneNumber").type(JsonFieldType.STRING).description("전화번호 (마스킹 없음, 형식: 010-XXXX-XXXX)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/users/me/edit - 인증 없이 접근 시 401 반환")
    void getMyInfoForEdit_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/users/me/edit")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /v1/users/me - 비밀번호/전화번호/이메일 동시 수정 성공")
    void updateMyInfo_success_all() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest(
            "NewPassword1!",
            "NewPassword1!",
            "010-9999-8888",
            "new@example.com"
        );

        willDoNothing().given(userService).updateMyInfo(anyLong(), any(MyInfoUpdateCommand.class));

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("회원 정보 수정에 성공하였습니다."))
            .andDo(document("user/update-my-info",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("마이페이지 회원정보 수정")
                    .description("""
                        마이페이지에서 비밀번호/전화번호/이메일을 한 번에 수정합니다.
                        모든 필드는 선택적이며 변경하려는 항목만 채워서 보내면 됩니다.
                        - 비밀번호: LOCAL 회원만 가능. 비밀번호와 비밀번호 확인이 일치해야 합니다.
                        - 전화번호: 기존 번호와 다른 경우에만 처리되며, /v1/sms/send/change → /v1/sms/verify 로 사전 인증 필요.
                        - 이메일: 형식 검증 후 그대로 갱신.
                        """)
                    .requestSchema(Schema.schema("MyInfoUpdateRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("password").type(JsonFieldType.STRING).optional().description("새 비밀번호 (8~16자, 영문/숫자/특수문자 중 2가지 이상 조합). 변경 안 하면 null"),
                        fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("새 비밀번호 확인. password와 동일해야 합니다."),
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).optional().description("새 전화번호 (010-XXXX-XXXX). 사전에 SMS 인증 필요. 기존 번호와 같으면 무시됩니다."),
                        fieldWithPath("email").type(JsonFieldType.STRING).optional().description("새 이메일")
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
    @DisplayName("PATCH /v1/users/me - 이메일만 수정 성공")
    void updateMyInfo_success_emailOnly() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest(null, null, null, "new@example.com");

        willDoNothing().given(userService).updateMyInfo(anyLong(), any(MyInfoUpdateCommand.class));

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /v1/users/me - 비밀번호 정규식 위반 시 400 반환")
    void updateMyInfo_invalidPasswordPattern() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest("short", "short", null, null);

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andDo(document("user/update-my-info-invalid-password",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("마이페이지 회원정보 수정 - 비밀번호 형식 오류")
                    .description("비밀번호가 정규식(8~16자, 2가지 이상 조합)을 만족하지 못하는 경우 400을 반환합니다.")
                    .requestSchema(Schema.schema("MyInfoUpdateRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("password").type(JsonFieldType.STRING).optional().description("새 비밀번호"),
                        fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("새 비밀번호 확인"),
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).optional().description("새 전화번호"),
                        fieldWithPath("email").type(JsonFieldType.STRING).optional().description("새 이메일")
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
    @DisplayName("PATCH /v1/users/me - 비밀번호와 비밀번호 확인 불일치 시 400 반환")
    void updateMyInfo_passwordConfirmMismatch() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest("Password1!", "Different1!", null, null);

        willThrow(new BusinessException(UserErrorCode.PASSWORD_CONFIRM_MISMATCH))
            .given(userService).updateMyInfo(anyLong(), any(MyInfoUpdateCommand.class));

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("비밀번호와 비밀번호 확인이 일치하지 않습니다."));
    }

    @Test
    @DisplayName("PATCH /v1/users/me - 소셜 회원이 비밀번호 변경 시 400 반환")
    void updateMyInfo_socialPasswordNotAllowed() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest("Password1!", "Password1!", null, null);

        willThrow(new BusinessException(UserErrorCode.SOCIAL_PASSWORD_NOT_ALLOWED))
            .given(userService).updateMyInfo(anyLong(), any(MyInfoUpdateCommand.class));

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("소셜 로그인 회원은 비밀번호를 변경할 수 없습니다."))
            .andDo(document("user/update-my-info-social-password",
                resource(ResourceSnippetParameters.builder()
                    .tag("User")
                    .summary("마이페이지 회원정보 수정 - 소셜 비밀번호 변경 거부")
                    .description("소셜 로그인 회원(KAKAO 등)이 비밀번호를 변경하려는 경우 400을 반환합니다.")
                    .requestSchema(Schema.schema("MyInfoUpdateRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("password").type(JsonFieldType.STRING).optional().description("새 비밀번호"),
                        fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).optional().description("새 비밀번호 확인"),
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).optional().description("새 전화번호"),
                        fieldWithPath("email").type(JsonFieldType.STRING).optional().description("새 이메일")
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
    @DisplayName("PATCH /v1/users/me - 전화번호 SMS 인증 미통과 시 401 반환")
    void updateMyInfo_smsNotVerified() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest(null, null, "010-9999-8888", null);

        willThrow(new BusinessException(SmsErrorCode.AUTH_CODE_EXPIRED))
            .given(userService).updateMyInfo(anyLong(), any(MyInfoUpdateCommand.class));

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("인증번호가 만료되었거나 일치하지 않습니다."));
    }

    @Test
    @DisplayName("PATCH /v1/users/me - 다른 회원이 사용 중인 전화번호로 변경 시 409 반환")
    void updateMyInfo_duplicatePhoneNumber() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest(null, null, "010-9999-8888", null);

        willThrow(new BusinessException(UserErrorCode.DUPLICATE_PHONE_NUMBER))
            .given(userService).updateMyInfo(anyLong(), any(MyInfoUpdateCommand.class));

        mockMvc.perform(patch("/v1/users/me")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 사용 중인 핸드폰 번호입니다."));
    }

    @Test
    @DisplayName("PATCH /v1/users/me - 인증 없이 접근 시 401 반환")
    void updateMyInfo_unauthorized() throws Exception {
        MyInfoUpdateRequest request = new MyInfoUpdateRequest(null, null, null, "new@example.com");

        mockMvc.perform(patch("/v1/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /v1/users/check/id - loginId가 4자 미만일 때 400 반환")
    void checkDuplicationId_invalidLoginId() throws Exception {
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
