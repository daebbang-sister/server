package com.daebbang.daebbangapi.domain.auth.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.handler.CustomLogoutHandler;
import com.daebbang.daebbangapi.handler.CustomLogoutSuccessHandler;
import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, LogoutTest.LogoutSecurityTestConfig.class})
class LogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtils cookieUtils;

    @TestConfiguration
    static class LogoutSecurityTestConfig {

        @Bean
        SecurityFilterChain logoutTestSecurityFilterChain(
            HttpSecurity http,
            AuthService authService,
            CookieUtils cookieUtils,
            ObjectMapper objectMapper
        ) throws Exception {
            CustomLogoutHandler handler = new CustomLogoutHandler(authService, cookieUtils);
            CustomLogoutSuccessHandler successHandler = new CustomLogoutSuccessHandler(objectMapper);

            http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/v1/tokens/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/v1/logout").permitAll()
                    .anyRequest().authenticated()
                )
                .logout(logout -> logout
                    .logoutRequestMatcher(
                        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.DELETE, "/v1/logout")
                    )
                    .addLogoutHandler(handler)
                    .logoutSuccessHandler(successHandler)
                    .permitAll()
                )
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

            return http.build();
        }
    }

    @Test
    @DisplayName("DELETE /v1/logout - 로그아웃 성공 (access token + refresh 쿠키)")
    void logout_success() throws Exception {
        // given
        willDoNothing().given(authService).logout(anyString(), anyString());
        willDoNothing().given(cookieUtils).expireRefreshCookie(any());

        // when & then
        mockMvc.perform(delete("/v1/logout")
                .header("Authorization", "Bearer valid-access-token")
                .cookie(new Cookie("refresh", "valid-refresh-token"))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."))
            .andDo(document("auth/logout",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("로그아웃")
                    .description("로그아웃 요청 시 access token 블랙리스트 등록, refresh token Redis 삭제, refresh 쿠키 만료 처리가 수행됩니다.")
                    .responseSchema(Schema.schema("LogoutResponse"))
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
    @DisplayName("DELETE /v1/logout - access token 없이 로그아웃 성공")
    void logout_withoutAccessToken() throws Exception {
        // given
        willDoNothing().given(authService).logout(any(), anyString());
        willDoNothing().given(cookieUtils).expireRefreshCookie(any());

        // when & then
        mockMvc.perform(delete("/v1/logout")
                .cookie(new Cookie("refresh", "valid-refresh-token"))
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."))
            .andDo(document("auth/logout-no-access-token",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("로그아웃 - access token 없음")
                    .description("Authorization 헤더 없이 refresh 쿠키만으로 로그아웃합니다. refresh token 삭제 및 쿠키 만료 처리만 수행됩니다.")
                    .responseSchema(Schema.schema("LogoutResponse"))
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
    @DisplayName("DELETE /v1/logout - refresh 쿠키 없이 로그아웃 성공")
    void logout_withoutRefreshCookie() throws Exception {
        // given
        willDoNothing().given(authService).logout(anyString(), any());
        willDoNothing().given(cookieUtils).expireRefreshCookie(any());

        // when & then
        mockMvc.perform(delete("/v1/logout")
                .header("Authorization", "Bearer valid-access-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("로그아웃에 성공하였습니다."))
            .andDo(document("auth/logout-no-refresh-cookie",
                resource(ResourceSnippetParameters.builder()
                    .tag("Auth")
                    .summary("로그아웃 - refresh 쿠키 없음")
                    .description("refresh 쿠키 없이 access token만으로 로그아웃합니다. access token 블랙리스트 등록 및 쿠키 만료 처리만 수행됩니다.")
                    .responseSchema(Schema.schema("LogoutResponse"))
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }
}
