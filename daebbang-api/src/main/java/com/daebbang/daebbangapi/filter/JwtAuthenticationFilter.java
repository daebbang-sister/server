package com.daebbang.daebbangapi.filter;

import com.daebbang.daebbangcore.infra.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");
        String token = jwtUtils.resolveToken(bearerToken);
        if (checkValidToken(token)) {
            Authentication authToken = getAuthentication(token);
            if (Objects.nonNull(authToken)) {
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkValidToken(String token) {
        return Objects.nonNull(token) && jwtUtils.validateToken(token);
    }
    private Authentication getAuthentication(String token) {
        String role = jwtUtils.getRole(token);
        String username = jwtUtils.getUserName(token);

        if (Objects.nonNull(role) && Objects.nonNull(username)) {
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        }
        return null;
    }
}
