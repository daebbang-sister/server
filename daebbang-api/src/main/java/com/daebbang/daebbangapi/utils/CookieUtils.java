package com.daebbang.daebbangapi.utils;

import com.daebbang.daebbangapi.domain.security.dto.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final CookieProperties properties;

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .secure(properties.secure())
            .sameSite(properties.sameSite())
            .domain(properties.domain())
            .maxAge(maxAge);

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        addCookie(response, name, "", 0);
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies).filter(c -> c.getName().equals(name)).findFirst();
    }

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

    public void expireRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh", "")
            .path("/")
            .httpOnly(true)
            .secure(properties.secure())
            .domain(properties.domain())
            .sameSite(properties.sameSite())
            .maxAge(0)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
