package com.daebbang.daebbangapi.filter;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcore.dto.LoginRequest;
import com.daebbang.daebbangcore.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@NullMarked
public class UserLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;
    private final AuthenticationManager authManager;

    public UserLoginFilter(AuthenticationManager authManager, ObjectMapper mapper, JwtUtils jwtUtils) {
        this.mapper = mapper;
        this.jwtUtils = jwtUtils;
        this.authManager = authManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {

        LoginRequest login = null;
        try {
            login = mapper.readValue(request.getInputStream(), LoginRequest.class);
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.JSON_CONVERT_ERROR);
        }

        return authManager.authenticate(new UsernamePasswordAuthenticationToken(login.id(), login.password()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authResult)
        throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException failed)
        throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
    }
}
