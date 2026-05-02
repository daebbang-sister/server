package com.daebbang.daebbangapi.domain.user.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.daebbang.daebbangapi.config.PasswordConfig;
import com.daebbang.daebbangapi.config.TestSecurityConfig;
import com.daebbang.daebbangapi.domain.user.dto.request.SmsSendRequest;
import com.daebbang.daebbangapi.domain.user.dto.request.SmsVerifyRequest;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcommon.error.SmsErrorCode;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.service.SmsService;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
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

@WebMvcTest(controllers = SmsController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class SmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SmsService smsService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /v1/sms/send - 인증번호 발송 성공")
    void sendAuthCode_success() throws Exception {
        // given
        SmsSendRequest request = new SmsSendRequest("010-1234-5678");
        String authCode = "123456";

        willDoNothing().given(userService).existsByPhoneNumber(anyString());
        given(smsService.sendAuthMessage(anyString())).willReturn(authCode);

        // when & then
        mockMvc.perform(post("/v1/sms/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("생성에 성공하였습니다."))
            .andExpect(jsonPath("$.data.authCode").value(authCode))
            .andDo(document("sms/send-auth-code",
                resource(ResourceSnippetParameters.builder()
                    .tag("SMS")
                    .summary("SMS 인증번호 발송")
                    .description("입력한 전화번호로 6자리 인증번호를 발송합니다. 이미 가입된 전화번호는 사용할 수 없습니다.")
                    .requestSchema(Schema.schema("SmsSendRequest"))
                    .responseSchema(Schema.schema("SmsSendResponse"))
                    .requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("인증번호를 받을 전화번호 (예: 010-1234-5678, 형식: 010-XXXX-XXXX)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("발송 결과"),
                        fieldWithPath("data.authCode").type(JsonFieldType.STRING).description("발송된 인증번호 (6자리)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/sms/send - 이미 가입된 전화번호로 발송 시 409 반환")
    void sendAuthCode_duplicatePhoneNumber() throws Exception {
        // given
        SmsSendRequest request = new SmsSendRequest("010-1234-5678");

        willThrow(new BusinessException(UserErrorCode.DUPLICATE_PHONE_NUMBER))
            .given(userService).existsByPhoneNumber(anyString());

        // when & then
        mockMvc.perform(post("/v1/sms/send")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.message").value("이미 사용 중인 핸드폰 번호입니다."))
            .andDo(document("sms/send-auth-code-duplicate",
                resource(ResourceSnippetParameters.builder()
                    .tag("SMS")
                    .summary("SMS 인증번호 발송 - 중복 전화번호 오류")
                    .description("이미 가입된 전화번호로 인증번호 발송 시 409 Conflict를 반환합니다.")
                    .requestSchema(Schema.schema("SmsSendRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("인증번호를 받을 전화번호 (예: 010-1234-5678, 형식: 010-XXXX-XXXX)")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (409)"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/sms/verify - 인증번호 검증 성공")
    void verifyAuthCode_success() throws Exception {
        // given
        SmsVerifyRequest request = new SmsVerifyRequest("010-1234-5678", "123456");

        willDoNothing().given(smsService).verifyAuthCode(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/v1/sms/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("핸드폰 인증에 성공하였습니다."))
            .andDo(document("sms/verify-auth-code",
                resource(ResourceSnippetParameters.builder()
                    .tag("SMS")
                    .summary("SMS 인증번호 검증")
                    .description("발송된 인증번호가 올바른지 검증합니다. 인증 성공 시 이후 회원 가입이 가능합니다.")
                    .requestSchema(Schema.schema("SmsVerifyRequest"))
                    .responseSchema(Schema.schema("SuccessResponse"))
                    .requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("인증번호를 받은 전화번호"),
                        fieldWithPath("authCode").type(JsonFieldType.STRING).description("수신한 6자리 인증번호")
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
    @DisplayName("POST /v1/sms/verify - 인증번호 불일치 시 400 반환")
    void verifyAuthCode_mismatch() throws Exception {
        // given
        SmsVerifyRequest request = new SmsVerifyRequest("010-1234-5678", "000000");

        willThrow(new BusinessException(SmsErrorCode.AUTH_CODE_MISMATCH))
            .given(smsService).verifyAuthCode(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/v1/sms/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("인증번호 코드가 일치하지 않습니다."))
            .andDo(document("sms/verify-auth-code-mismatch",
                resource(ResourceSnippetParameters.builder()
                    .tag("SMS")
                    .summary("SMS 인증번호 검증 - 불일치 오류")
                    .description("인증번호가 일치하지 않는 경우 400 Bad Request를 반환합니다.")
                    .requestSchema(Schema.schema("SmsVerifyRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("인증번호를 받은 전화번호"),
                        fieldWithPath("authCode").type(JsonFieldType.STRING).description("수신한 6자리 인증번호")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드 (400)"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                        fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/sms/verify - 인증번호 만료 시 401 반환")
    void verifyAuthCode_expired() throws Exception {
        // given
        SmsVerifyRequest request = new SmsVerifyRequest("010-1234-5678", "123456");

        willThrow(new BusinessException(SmsErrorCode.AUTH_CODE_EXPIRED))
            .given(smsService).verifyAuthCode(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/v1/sms/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("인증번호가 만료되었거나 일치하지 않습니다."))
            .andDo(document("sms/verify-auth-code-expired",
                resource(ResourceSnippetParameters.builder()
                    .tag("SMS")
                    .summary("SMS 인증번호 검증 - 만료 오류")
                    .description("인증번호가 만료된 경우 401 Unauthorized를 반환합니다.")
                    .requestSchema(Schema.schema("SmsVerifyRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestFields(
                        fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("인증번호를 받은 전화번호"),
                        fieldWithPath("authCode").type(JsonFieldType.STRING).description("수신한 6자리 인증번호")
                    )
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
