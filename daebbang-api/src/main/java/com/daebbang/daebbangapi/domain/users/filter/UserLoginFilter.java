package com.daebbang.daebbangapi.domain.users.filter;

import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcommon.dto.authority.TokenInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcommon.util.ValidationUtils;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import com.daebbang.daebbangcore.domain.user.dto.request.LoginRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@NullMarked
public class UserLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper mapper;
    private final CookieUtils cookieUtils;
    private final AuthService authService;
    private final AuthenticationManager authManager;

    public UserLoginFilter(AuthenticationManager authManager, ObjectMapper mapper, CookieUtils cookieUtils, AuthService authService) {
        this.mapper = mapper;
        this.authManager = authManager;
        this.cookieUtils = cookieUtils;
        this.authService = authService;

        setFilterProcessesUrl("/v1/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {

        LoginRequest login = null;
        try {
            login = mapper.readValue(request.getInputStream(), LoginRequest.class);
            ValidationUtils.validateLoginRequest(login.id(), login.password());
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_CONVERT_ERROR);
        }

        return authManager.authenticate(new UsernamePasswordAuthenticationToken(login.id(), login.password()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {
        UserDetails user = (UserDetails) authResult.getPrincipal();
        if (Objects.isNull(user)) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }

        String userRole = user.getAuthorities().iterator().next().getAuthority();

        TokenInfo info = authService.issueTokenAndSaveToken(user.getUsername(), userRole);

        cookieUtils.addRefreshCookie(response, info.refreshToken());

        writeResponse(
            response, mapper,
            HttpStatus.OK,
            CommonResponse.success(
                CommonSuccessCode.CREATE_SUCCESS,
                TokenInfo.create(info.accessToken())
            )
        );
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed)
        throws IOException, ServletException {
        CommonResponse<?> error = CommonResponse.error(UserErrorCode.INVALID_LOGIN_INFO);
        writeResponse(response, mapper, HttpStatus.UNAUTHORIZED, error);
    }

    private void writeResponse(
        HttpServletResponse response, ObjectMapper mapper,
        HttpStatus status, CommonResponse<?> body) throws IOException {

        response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());

        String bodyResponse = mapper.writeValueAsString(body);
        response.getWriter().write(bodyResponse);
    }
}
