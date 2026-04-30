package com.daebbang.daebbangapi.domain.address.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.address.entity.AddressVO;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.domain.user.service.UserService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private Oauth2UserDetailsService oauth2UserDetailsService;

    private static final UsernamePasswordAuthenticationToken AUTH =
        new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

    private Address mockAddress(boolean isDefault) {
        Users user = Users.createLocalUser("testuser", "encoded", "홍길동", "test@example.com", "01012345678");
        AddressVO addressVO = AddressVO.of("12345", "서울시 강남구 테헤란로", "101호");
        return Address.create(user, "홍길동", "01012345678", "집", addressVO, isDefault);
    }

    // ===== GET /v1/addresses =====

    @Test
    @DisplayName("GET /v1/addresses - 주소 목록 조회 성공")
    void getAddresses_success() throws Exception {
        given(addressService.findAllByUserId(anyLong()))
            .willReturn(List.of(mockAddress(true), mockAddress(false)));

        mockMvc.perform(get("/v1/addresses")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("주소 목록 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].isDefault").value(true))
            .andExpect(jsonPath("$.data[1].isDefault").value(false))
            .andDo(document("address/get-addresses",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 목록 조회")
                    .description("인증된 회원의 전체 주소 목록을 조회합니다.")
                    .responseSchema(Schema.schema("AddressListResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("주소 목록"),
                        fieldWithPath("data[].addressId").type(JsonFieldType.VARIES).description("주소 ID"),
                        fieldWithPath("data[].alias").type(JsonFieldType.VARIES).optional().description("주소 별칭 (null 가능)"),
                        fieldWithPath("data[].receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("data[].receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 전화번호"),
                        fieldWithPath("data[].zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("data[].address").type(JsonFieldType.STRING).description("도로명 주소"),
                        fieldWithPath("data[].detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("data[].isDefault").type(JsonFieldType.BOOLEAN).description("기본 배송지 여부")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/addresses - 주소가 없을 때 빈 배열 반환")
    void getAddresses_empty() throws Exception {
        given(addressService.findAllByUserId(anyLong())).willReturn(List.of());

        mockMvc.perform(get("/v1/addresses")
                .with(authentication(AUTH))
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0))
            .andDo(document("address/get-addresses-empty",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 목록 조회 - 빈 목록")
                    .description("등록된 주소가 없을 경우 빈 배열을 반환합니다.")
                    .responseSchema(Schema.schema("AddressListResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("주소 목록 (빈 배열)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("GET /v1/addresses - 인증 없이 접근 시 401 반환")
    void getAddresses_unauthorized() throws Exception {
        mockMvc.perform(get("/v1/addresses")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    // ===== POST /v1/addresses =====

    @Test
    @DisplayName("POST /v1/addresses - 주소 등록 성공")
    void addAddress_success() throws Exception {
        Users user = Users.createLocalUser("testuser", "encoded", "홍길동", "test@example.com", "01012345678");
        given(userService.getUserById(anyLong())).willReturn(user);
        willDoNothing().given(addressService).save(any(Users.class), any(AddressCommand.class));

        Map<String, Object> request = Map.of(
            "receiver", "홍길동",
            "receiverPhoneNumber", "010-1234-5678",
            "alias", "집",
            "zipCode", "12345",
            "address", "서울시 강남구 테헤란로",
            "detailAddress", "101호",
            "isDefault", true
        );

        mockMvc.perform(post("/v1/addresses")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.message").value("새 주소 등록에 성공하였습니다."))
            .andDo(document("address/add-address",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 등록")
                    .description("새로운 배송지를 등록합니다. 최대 5개까지 등록 가능하며, isDefault가 true이면 기존 기본 배송지가 해제됩니다.")
                    .requestSchema(Schema.schema("AddressSaveRequest"))
                    .responseSchema(Schema.schema("CommonResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 전화번호 (010-XXXX-XXXX)"),
                        fieldWithPath("alias").type(JsonFieldType.STRING).optional().description("주소 별칭 (예: 집, 회사)"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("도로명 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("isDefault").type(JsonFieldType.BOOLEAN).description("기본 배송지 여부")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("POST /v1/addresses - 필수 필드 누락 시 400 반환")
    void addAddress_validationFail() throws Exception {
        Map<String, Object> request = Map.of(
            "isDefault", true
        );

        mockMvc.perform(post("/v1/addresses")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /v1/addresses - 주소 5개 초과 시 409 반환")
    void addAddress_limitExceeded() throws Exception {
        Users user = Users.createLocalUser("testuser", "encoded", "홍길동", "test@example.com", "01012345678");
        given(userService.getUserById(anyLong())).willReturn(user);
        willThrow(new BusinessException(UserErrorCode.ADDRESS_LIMIT_EXCEEDED))
            .given(addressService).save(any(Users.class), any(AddressCommand.class));

        Map<String, Object> request = Map.of(
            "receiver", "홍길동",
            "receiverPhoneNumber", "010-1234-5678",
            "zipCode", "12345",
            "address", "서울시 강남구 테헤란로",
            "detailAddress", "101호",
            "isDefault", false
        );

        mockMvc.perform(post("/v1/addresses")
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andDo(document("address/add-address-limit-exceeded",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 등록 실패 - 최대 개수 초과")
                    .description("주소록은 최대 5개까지 등록 가능합니다.")
                    .requestSchema(Schema.schema("AddressSaveRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    // ===== PUT /v1/addresses/{addressId} =====

    @Test
    @DisplayName("PUT /v1/addresses/{addressId} - 주소 수정 성공")
    void updateAddress_success() throws Exception {
        willDoNothing().given(addressService).update(anyLong(), anyLong(), any(AddressCommand.class));

        Map<String, Object> request = Map.of(
            "receiver", "김철수",
            "receiverPhoneNumber", "010-9876-5432",
            "alias", "회사",
            "zipCode", "54321",
            "address", "서울시 서초구 반포대로",
            "detailAddress", "202호",
            "isDefault", false
        );

        mockMvc.perform(put("/v1/addresses/{addressId}", 1L)
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("주소 수정에 성공하였습니다."))
            .andDo(document("address/update-address",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 수정")
                    .description("기존 배송지 정보를 수정합니다. isDefault가 true이면 기존 기본 배송지가 해제됩니다.")
                    .requestSchema(Schema.schema("AddressUpdateRequest"))
                    .responseSchema(Schema.schema("CommonResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .requestFields(
                        fieldWithPath("receiver").type(JsonFieldType.STRING).description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").type(JsonFieldType.STRING).description("수령인 전화번호 (010-XXXX-XXXX)"),
                        fieldWithPath("alias").type(JsonFieldType.STRING).optional().description("주소 별칭 (예: 집, 회사)"),
                        fieldWithPath("zipCode").type(JsonFieldType.STRING).description("우편번호"),
                        fieldWithPath("address").type(JsonFieldType.STRING).description("도로명 주소"),
                        fieldWithPath("detailAddress").type(JsonFieldType.STRING).description("상세 주소"),
                        fieldWithPath("isDefault").type(JsonFieldType.BOOLEAN).description("기본 배송지 여부")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("PUT /v1/addresses/{addressId} - 존재하지 않는 주소 수정 시 404 반환")
    void updateAddress_notFound() throws Exception {
        willThrow(new BusinessException(UserErrorCode.ADDRESS_NOT_FOUND))
            .given(addressService).update(anyLong(), eq(999L), any(AddressCommand.class));

        Map<String, Object> request = Map.of(
            "receiver", "홍길동",
            "receiverPhoneNumber", "010-1234-5678",
            "zipCode", "12345",
            "address", "서울시 강남구 테헤란로",
            "detailAddress", "101호",
            "isDefault", false
        );

        mockMvc.perform(put("/v1/addresses/{addressId}", 999L)
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andDo(document("address/update-address-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 수정 실패 - 주소 없음")
                    .description("존재하지 않는 주소 ID로 수정 요청 시 404를 반환합니다.")
                    .requestSchema(Schema.schema("AddressUpdateRequest"))
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    // ===== DELETE /v1/addresses/{addressId} =====

    @Test
    @DisplayName("DELETE /v1/addresses/{addressId} - 주소 삭제 성공")
    void deleteAddress_success() throws Exception {
        willDoNothing().given(addressService).deleteById(anyLong(), anyLong());

        mockMvc.perform(delete("/v1/addresses/{addressId}", 1L)
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("주소 삭제에 성공하였습니다."))
            .andDo(document("address/delete-address",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 삭제")
                    .description("배송지를 삭제합니다 (소프트 삭제).")
                    .responseSchema(Schema.schema("CommonResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }

    @Test
    @DisplayName("DELETE /v1/addresses/{addressId} - 존재하지 않는 주소 삭제 시 404 반환")
    void deleteAddress_notFound() throws Exception {
        willThrow(new BusinessException(UserErrorCode.ADDRESS_NOT_FOUND))
            .given(addressService).deleteById(anyLong(), eq(999L));

        mockMvc.perform(delete("/v1/addresses/{addressId}", 999L)
                .with(authentication(AUTH))
                .with(csrf())
                .header("Authorization", "Bearer test-jwt-token")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andDo(document("address/delete-address-not-found",
                resource(ResourceSnippetParameters.builder()
                    .tag("Address")
                    .summary("주소 삭제 실패 - 주소 없음")
                    .description("존재하지 않는 주소 ID로 삭제 요청 시 404를 반환합니다.")
                    .responseSchema(Schema.schema("ErrorResponse"))
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer JWT 토큰")
                    )
                    .responseFields(
                        fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                        fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                        fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지"),
                        fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                    )
                    .build()
                )));
    }
}
