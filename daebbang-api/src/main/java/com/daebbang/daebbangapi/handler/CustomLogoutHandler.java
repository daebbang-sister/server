package com.daebbang.daebbangapi.handler;

import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
@NullMarked
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        @Nullable Authentication authentication) {

        String bearerToken = request.getHeader("Authorization");
        String accessToken = (bearerToken != null && bearerToken.startsWith("Bearer "))
            ? bearerToken.substring(7) : null;

        Cookie refreshCookie = WebUtils.getCookie(request, "refresh");
        String refreshToken = refreshCookie != null ? refreshCookie.getValue() : null;

        authService.logout(accessToken, refreshToken);
        cookieUtils.expireRefreshCookie(response);
    }
}
