package com.daebbang.daebbangapi.utils;

import com.daebbang.daebbangapi.domain.security.dto.CookieProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final CookieProperties properties;

    public void addRefreshCookie(HttpServletResponse response, String value) {
        ResponseCookie cookie = ResponseCookie.from("refresh", value)
            .path("/")
            .httpOnly(true)
            .secure(properties.secure())
            .domain(properties.domain())
            .sameSite(properties.sameSite())
            .maxAge(properties.maxAge())
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
