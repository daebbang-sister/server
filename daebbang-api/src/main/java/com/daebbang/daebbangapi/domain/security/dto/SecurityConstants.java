package com.daebbang.daebbangapi.domain.security.dto;

public abstract class SecurityConstants {
    public static final String[] PUBLIC_GET_URI = {
        "/v1/users/find/**",
        "/v1/users/check/**",
        "/v1/products/**",
        "/v1/categories/**",
        "/v1/products/*/reviews",
        "/v1/products/*/reviews/stats"
    };

    public static final String[] PUBLIC_POST_URI = {
        "/v1/users",
        "/v1/sms/**",
        "/v1/auth/login",
        "/v1/tokens/reissues",
        "/v1/users/find/password"
    };

    public static final String[] SWAGGER_URI = {
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    };
}
