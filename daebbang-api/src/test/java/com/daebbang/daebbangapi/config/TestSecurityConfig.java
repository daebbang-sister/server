package com.daebbang.daebbangapi.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-only SecurityFilterChain that replaces the main SecurityConfig for @WebMvcTest.
 *
 * <p>The main SecurityConfig configures oauth2Login which sets a redirect-based
 * AuthenticationEntryPoint (→ /oauth2/authorization/kakao). This causes even
 * permitAll() endpoints to receive 302 in the test context.
 *
 * <p>This configuration has higher priority (@Order(0) vs default @Order(100)) so it
 * handles all requests in tests. It returns HTTP 401 instead of redirecting for
 * unauthenticated requests, and has no OAuth2 login or custom filters.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/v1/sms/send").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/sms/verify").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/users/find/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/users/find/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/users/check/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/tokens/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/categories/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
}
