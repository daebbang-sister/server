package com.daebbang.daebbangapi.domain.auth.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcommon.dto.authority.TokenInfo;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.AuthErrorCode;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import jakarta.servlet.http.Cookie;
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

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtils cookieUtils;

    @Test
    @DisplayName("POST /v1/tokens/reissues - 토큰 재발급 성공")
    void reissueTokens_success() throws Exception {
        // given
        String oldRefreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";

        TokenInfo tokenInfo = TokenInfo.create("Bearer", newAccessToken, newRefreshToken);
        given(authService.rotateRefreshToken(oldRefreshToken)).willReturn(tokenInfo);
        willDoNothing().given(cookieUtils).addRefreshCookie(any(), anyString());

        // when & then
        mockMvc.perform(post("/v1/tokens/reissues")
                .cookie(new Cookie("refresh", oldRefreshToken))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("생성에 성공하였습니다."))
            .andExpect(jsonPath("$.data.grantType").value("Bearer"))
            .andExpect(jsonPath("$.data.accessToken").value(newAccessToken))
            .andDo(document("auth/reissue-tokens",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("토큰 재발급")
                    .description("Refresh 토큰 쿠키를 이용해 새로운 Access 토큰과 Refresh 토큰을 재발급합니다. 새로운 Refresh 토큰은 Set-Cookie 헤더로 반환됩니다.")
                    .responseSchema(Schema.schema("ReissueTokenResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("토큰 정보"),
                        fieldWithPath("data.grantType").type(JsonFieldType.STRING).description("토큰 타입 (Bearer)"),
                        fieldWithPath("data.accessToken").type(JsonFieldType.STRING).description("새로 발급된 Access 토큰"),
                        fieldWithPath("data.refreshToken").type(JsonFieldType.VARIES).optional().description("Refresh 토큰 (응답 바디에는 포함되지 않음, Set-Cookie로 전달)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/tokens/reissues - Refresh 쿠키 없이 요청 시 400 반환")
    void reissueTokens_missingCookie() throws Exception {
        mockMvc.perform(post("/v1/tokens/reissues")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andDo(document("auth/reissue-tokens-missing-cookie",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("토큰 재발급 - refresh 쿠키 누락")
                    .description("refresh 쿠키가 없는 경우 400 Bad Request를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).optional().description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).optional().description("HTTP 상태 코드 (400)"),
                        fieldWithPath("message").type(JsonFieldType.STRING).optional().description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/tokens/reissues - 만료된 Refresh 토큰으로 요청 시 401 반환")
    void reissueTokens_expiredToken() throws Exception {
        // given
        String expiredToken = "expired-refresh-token";

        willThrow(new BusinessException(AuthErrorCode.EXPIRED_TOKEN))
            .given(authService).rotateRefreshToken(expiredToken);

        // when & then
        mockMvc.perform(post("/v1/tokens/reissues")
                .cookie(new Cookie("refresh", expiredToken))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("만료된 토큰입니다."))
            .andDo(document("auth/reissue-tokens-expired",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("토큰 재발급 - refresh 토큰 만료")
                    .description("Refresh 토큰이 만료된 경우 401 Unauthorized를 반환합니다. 다시 로그인해야 합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (401)"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/tokens/reissues - 유효하지 않은 Refresh 토큰으로 요청 시 401 반환")
    void reissueTokens_invalidToken() throws Exception {
        // given
        String invalidToken = "invalid-refresh-token";

        willThrow(new BusinessException(AuthErrorCode.INVALID_TOKEN))
            .given(authService).rotateRefreshToken(invalidToken);

        // when & then
        mockMvc.perform(post("/v1/tokens/reissues")
                .cookie(new Cookie("refresh", invalidToken))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."))
            .andDo(document("auth/reissue-tokens-invalid",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("토큰 재발급 - 유효하지 않은 refresh 토큰")
                    .description("Refresh 토큰이 유효하지 않거나 Redis에 저장된 토큰과 일치하지 않는 경우 401 Unauthorized를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (401)"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }
}
