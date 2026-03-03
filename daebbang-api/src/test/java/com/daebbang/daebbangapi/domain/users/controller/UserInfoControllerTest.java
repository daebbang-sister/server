package com.daebbang.daebbangapi.domain.users.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.users.dto.request.UserPasswordFindRequest;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.EmailService;
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
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserInfoController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
public class UserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("GET /v1/users/find/id - 아이디 찾기 성공")
    void findUserId_success() throws Exception {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        Users mockUser = Users.createLocalUser("testuser123", "encoded_pw", name, email, "01012345678");

        given(userService.getUsersByFindLoginId(name, email)).willReturn(List.of(mockUser));

        // when & then
        mockMvc.perform(get("/v1/users/find/id")
                .param("username", name)
                .param("userEmail", email)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("회원 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.userIds").isArray())
            .andExpect(jsonPath("$.data.userIds[0].id").value("tes********"))
            .andExpect(jsonPath("$.data.userIds[0].provider").value("LOCAL"))
            .andDo(document("user-info/find-login-id",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("아이디 찾기")
                    .description("이름과 이메일로 마스킹 처리된 로그인 아이디 목록을 조회합니다.")
                    .responseSchema(Schema.schema("FindUserIdResponse"))
                    .queryParameters(
                        parameterWithName("username").description("회원 이름"),
                        parameterWithName("userEmail").description("이메일 주소")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("아이디 찾기 결과"),
                        fieldWithPath("data.userIds").type(JsonFieldType.ARRAY).description("아이디 목록 (일치하는 회원이 없으면 빈 배열)"),
                        fieldWithPath("data.userIds[].id").type(JsonFieldType.STRING).description("마스킹 처리된 로그인 아이디 (앞 3자리 + ********, 예: tes********)"),
                        fieldWithPath("data.userIds[].provider").type(JsonFieldType.STRING).description("로그인 제공자 (LOCAL, KAKAO)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/users/find/id - 일치하는 회원이 없을 시 빈 목록 반환")
    void findUserId_emptyResult() throws Exception {
        // given
        String name = "없는사람";
        String email = "notfound@example.com";

        given(userService.getUsersByFindLoginId(name, email)).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/v1/users/find/id")
                .param("username", name)
                .param("userEmail", email)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.userIds").isEmpty())
            .andDo(document("user-info/find-login-id-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("아이디 찾기 - 결과 없음")
                    .description("이름과 이메일로 조회했을 때 일치하는 회원이 없으면 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("FindUserIdResponse"))
                    .queryParameters(
                        parameterWithName("username").description("회원 이름"),
                        parameterWithName("userEmail").description("이메일 주소")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("아이디 찾기 결과"),
                        fieldWithPath("data.userIds").type(JsonFieldType.ARRAY).description("아이디 목록 (일치하는 회원이 없으면 빈 배열)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/users/find/id - username이 빈 값일 때 400 반환")
    void findUserId_blankUsername() throws Exception {
        mockMvc.perform(get("/v1/users/find/id")
                .param("username", "")
                .param("userEmail", "test@example.com")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user-info/find-login-id-blank-username",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("아이디 찾기 - username 누락 오류")
                    .description("username이 빈 값인 경우 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .queryParameters(
                        parameterWithName("username").description("회원 이름 (빈 값)"),
                        parameterWithName("userEmail").description("이메일 주소")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (400)"),
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
    @DisplayName("GET /v1/users/find/id - userEmail 형식 불일치 시 400 반환")
    void findUserId_invalidEmail() throws Exception {
        mockMvc.perform(get("/v1/users/find/id")
                .param("username", "홍길동")
                .param("userEmail", "invalid-email")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user-info/find-login-id-invalid-email",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("아이디 찾기 - userEmail 형식 오류")
                    .description("userEmail이 이메일 형식이 아닌 경우 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .queryParameters(
                        parameterWithName("username").description("회원 이름"),
                        parameterWithName("userEmail").description("이메일 주소 (형식 불일치)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (400)"),
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
    @DisplayName("POST /v1/users/find/password - 비밀번호 찾기 이메일 발송 성공")
    void findPassword_success() throws Exception {
        // given
        UserPasswordFindRequest request = new UserPasswordFindRequest("홍길동", "testuser123", "test@example.com");

        given(userService.existsActiveUsers("홍길동", "testuser123", "test@example.com")).willReturn(true);
        willDoNothing().given(emailService).sendTemporaryPassword("test@example.com");

        // when & then
        mockMvc.perform(post("/v1/users/find/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("이메일 전송에 성공하였습니다."))
            .andDo(document("user-info/find-password",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("비밀번호 찾기 이메일 발송")
                    .description("이름, 로그인 ID, 이메일로 본인 확인 후 비밀번호 재설정 이메일을 발송합니다.")
                    .requestSchema(Schema.schema("FindPasswordRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("userId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("userEmail").type(JsonFieldType.STRING).description("이메일 주소")
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
    @DisplayName("POST /v1/users/find/password - username이 빈 값일 때 400 반환")
    void findPassword_blankUsername() throws Exception {
        // given
        UserPasswordFindRequest request = new UserPasswordFindRequest("", "testuser123", "test@example.com");

        // when & then
        mockMvc.perform(post("/v1/users/find/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user-info/find-password-blank-username",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("비밀번호 찾기 - username 누락 오류")
                    .description("username이 빈 값인 경우 400 Bad Request를 반환합니다.")
                    .requestSchema(Schema.schema("FindPasswordRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING).description("회원 이름 (빈 값)"),
                        fieldWithPath("userId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("userEmail").type(JsonFieldType.STRING).description("이메일 주소")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (400)"),
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
    @DisplayName("POST /v1/users/find/password - userId가 4자 미만일 때 400 반환")
    void findPassword_invalidUserId() throws Exception {
        // given
        UserPasswordFindRequest request = new UserPasswordFindRequest("홍길동", "abc", "test@example.com");

        // when & then
        mockMvc.perform(post("/v1/users/find/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user-info/find-password-invalid-user-id",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("비밀번호 찾기 - userId 길이 오류")
                    .description("userId가 4자 미만이거나 16자 초과인 경우 400 Bad Request를 반환합니다.")
                    .requestSchema(Schema.schema("FindPasswordRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("userId").type(JsonFieldType.STRING).description("로그인 ID (4자 미만)"),
                        fieldWithPath("userEmail").type(JsonFieldType.STRING).description("이메일 주소")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (400)"),
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
    @DisplayName("POST /v1/users/find/password - userEmail 형식 불일치 시 400 반환")
    void findPassword_invalidUserEmail() throws Exception {
        // given
        UserPasswordFindRequest request = new UserPasswordFindRequest("홍길동", "testuser123", "invalid-email");

        // when & then
        mockMvc.perform(post("/v1/users/find/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("잘못된 입력값입니다."))
            .andDo(document("user-info/find-password-invalid-email",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("비밀번호 찾기 - userEmail 형식 오류")
                    .description("userEmail이 이메일 형식이 아닌 경우 400 Bad Request를 반환합니다.")
                    .requestSchema(Schema.schema("FindPasswordRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("userId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("userEmail").type(JsonFieldType.STRING).description("이메일 주소 (형식 불일치)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (400)"),
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
    @DisplayName("POST /v1/users/find/password - 존재하지 않는 회원 정보로 요청 시 404 반환")
    void findPassword_userNotFound() throws Exception {
        // given
        UserPasswordFindRequest request = new UserPasswordFindRequest("홍길동", "testuser123", "test@example.com");

        given(userService.existsActiveUsers("홍길동", "testuser123", "test@example.com")).willReturn(false);

        // when & then
        mockMvc.perform(post("/v1/users/find/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
            .andDo(document("user-info/find-password-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("UserInfo")
                    .summary("비밀번호 찾기 이메일 발송 - 회원 없음 오류")
                    .description("이름, 로그인 ID, 이메일에 일치하는 활성 회원이 없는 경우 404 Not Found를 반환합니다.")
                    .requestSchema(Schema.schema("FindPasswordRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("username").type(JsonFieldType.STRING).description("회원 이름"),
                        fieldWithPath("userId").type(JsonFieldType.STRING).description("로그인 ID"),
                        fieldWithPath("userEmail").type(JsonFieldType.STRING).description("이메일 주소")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (404)"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }
}
