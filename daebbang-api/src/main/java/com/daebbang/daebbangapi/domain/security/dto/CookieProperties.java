package com.daebbang.daebbangapi.domain.security.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.app.cookie")
public record CookieProperties(
    boolean secure,
    String sameSite,
    String domain,
    long maxAge
) {

}
