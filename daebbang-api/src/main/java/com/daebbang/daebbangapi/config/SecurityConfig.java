package com.daebbang.daebbangapi.config;

import com.daebbang.daebbangapi.filter.CustomExceptionFilter;
import com.daebbang.daebbangapi.filter.JwtAuthenticationFilter;
import com.daebbang.daebbangapi.domain.users.filter.UserLoginFilter;
import com.daebbang.daebbangapi.handler.OAuth2LoginSuccessHandler;
import com.daebbang.daebbangapi.provider.TokenProvider;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;
    private final TokenProvider tokenProvider;
    private final PasswordConfig passwordConfig;

    private final CustomUserDetailsService userService;
    private final Oauth2UserDetailsService oauth2Service;

    private static final String[] SWAGGER_URI = {
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    };

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService service, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers(SWAGGER_URI);
    }


    @Bean
    public SecurityFilterChain doChain(HttpSecurity http) throws Exception {

        AuthenticationManager manager = authenticationManager(userService, passwordConfig.passwordEncoder());

        http
            .csrf(AbstractHttpConfigurer::disable);

        http
            .formLogin(AbstractHttpConfigurer::disable);

        http
            .httpBasic(AbstractHttpConfigurer::disable);

        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers(SWAGGER_URI).permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/sms/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/users/find/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/users/find/password").permitAll()
                .anyRequest().authenticated()
            );

        http
            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((config) -> config
                    .userService(oauth2Service))
                .successHandler(new OAuth2LoginSuccessHandler(mapper, tokenProvider))
            );

        http
            .addFilterBefore(new CustomExceptionFilter(mapper), UserLoginFilter.class)
            .addFilterAt(new UserLoginFilter(manager, mapper, tokenProvider), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtils, mapper), UsernamePasswordAuthenticationFilter.class);

        http
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}