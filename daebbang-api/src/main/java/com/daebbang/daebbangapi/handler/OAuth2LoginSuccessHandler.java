package com.daebbang.daebbangapi.handler;

import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcommon.dto.authority.TokenInfo;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@NullMarked
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final CookieUtils cookieUtils;
    private final AuthService authService;

    @Value("${spring.oauth2.success}")
    private String successRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String username = Objects.requireNonNull(user).getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        TokenInfo info = authService.issueTokenAndSaveToken(username, role);

        cookieUtils.addRefreshCookie(response, info.refreshToken());

        getRedirectStrategy().sendRedirect(request, response, successRedirectUri);
    }
}
