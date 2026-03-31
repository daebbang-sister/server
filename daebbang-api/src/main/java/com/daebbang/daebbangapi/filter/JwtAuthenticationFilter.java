package com.daebbang.daebbangapi.filter;

import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.error.ErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.infra.service.RedisService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final ObjectMapper mapper;

    private final RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");
        String token = jwtUtils.resolveToken(bearerToken);
        try {
            if (checkValidToken(token)) {
                if (isBlackListUser(token)) {
                    sendErrorResponse(response, UserErrorCode.EXPIRED_TOKEN);
                    return;
                }

                Authentication authToken = getAuthentication(token);
                if (Objects.nonNull(authToken)) {
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (SecurityException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
            sendErrorResponse(response, UserErrorCode.INVALID_TOKEN);
            return;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.", e);
            sendErrorResponse(response, UserErrorCode.EXPIRED_TOKEN);
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않은 JWT 토큰 형식입니다.", e);
            sendErrorResponse(response, UserErrorCode.UNSUPPORTED_TOKEN);
            return;
        } catch (Exception e) {
            log.warn("JWT 토큰이 비어있거나 잘못되었습니다.");
            sendErrorResponse(response, UserErrorCode.INVALID_TOKEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkValidToken(String token) {
        return Objects.nonNull(token) && jwtUtils.validateToken(token);
    }

    private boolean isBlackListUser(String token) {
        return redisService.hasKey("BL:" + token);
    }

    private Authentication getAuthentication(String token) {
        String role = jwtUtils.getRole(token);
        Long userId = jwtUtils.getUserId(token);

        if (Objects.nonNull(role) && Objects.nonNull(userId)) {
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            return new UsernamePasswordAuthenticationToken(userId, null, authorities);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        CommonResponse<Void> errorResponse = CommonResponse.error(errorCode);
        String json = mapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
