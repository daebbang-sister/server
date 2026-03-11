package com.daebbang.daebbangapi.config;

import com.daebbang.daebbangapi.domain.security.dto.SecurityConstants;
import com.daebbang.daebbangapi.filter.CustomExceptionFilter;
import com.daebbang.daebbangapi.filter.JwtAuthenticationFilter;
import com.daebbang.daebbangapi.domain.users.filter.UserLoginFilter;
import com.daebbang.daebbangapi.handler.OAuth2LoginFailureHandler;
import com.daebbang.daebbangapi.handler.OAuth2LoginSuccessHandler;
import com.daebbang.daebbangapi.domain.oauth.service.oauth2.Oauth2UserDetailsService;
import com.daebbang.daebbangapi.domain.users.service.CustomUserDetailsService;
import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;
    private final PasswordConfig passwordConfig;
    private final AccessDeniedHandler accessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    private final CookieUtils cookieUtils;

    private final AuthService authService;
    private final CustomUserDetailsService userService;
    private final Oauth2UserDetailsService oauth2Service;

    @Bean
    public AuthenticationManager authenticationManager(CustomUserDetailsService service, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .requestMatchers(SecurityConstants.SWAGGER_URI);
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
            .cors(cors -> cors.configurationSource(corsConfigurationSource));

        http
            .authorizeHttpRequests((auth) -> auth
                .requestMatchers(SecurityConstants.SWAGGER_URI).permitAll()
                .requestMatchers(HttpMethod.POST, SecurityConstants.PUBLIC_POST_URI).permitAll()
                .requestMatchers(HttpMethod.GET, SecurityConstants.PUBLIC_GET_URI).permitAll()
                .anyRequest().authenticated()
            );

        http
            .oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((config) -> config
                    .userService(oauth2Service))
                .successHandler(new OAuth2LoginSuccessHandler(cookieUtils, authService))
                .failureHandler(new OAuth2LoginFailureHandler())
            );

        http
            .exceptionHandling(conf -> conf
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        http
            .addFilterBefore(new CustomExceptionFilter(mapper), UserLoginFilter.class)
            .addFilterAt(new UserLoginFilter(manager, mapper, cookieUtils, authService), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtils, mapper), UsernamePasswordAuthenticationFilter.class);

        http
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

}