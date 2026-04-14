package com.daebbang.daebbangapi.domain.address.controller;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
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
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangcore.domain.address.entity.Address;
import com.daebbang.daebbangcore.domain.address.entity.AddressVO;
import com.daebbang.daebbangcore.domain.address.service.AddressService;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import static com.epages.restdocs.apispec.ResourceDocumentation.headerWithName;
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

@WebMvcTest(controllers = AddressController.class)
@AutoConfigureRestDocs
@ActiveProfiles("test")
@Import({PasswordConfig.class, TestSecurityConfig.class})
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Address mockAddress(boolean isDefault) {
        Users user = Users.createLocalUser("testuser", "encoded", "홍길동", "test@example.com", "01012345678");
        AddressVO addressVO = AddressVO.of("12345", "서울시 강남구 테헤란로", "101호");
        return Address.create(user, "홍길동", "01012345678", "집", addressVO, isDefault);
    }

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
}
