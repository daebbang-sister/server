package com.daebbang.daebbangcore.domain.auth.service.impl;

import com.daebbang.daebbangcommon.dto.authority.TokenInfo;
import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import com.daebbang.daebbangcore.infra.service.RedisService;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    @Override
    @Transactional
    public TokenInfo rotateRefreshToken(String refreshToken) {

        // refresh 검증
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException(UserErrorCode.EXPIRED_TOKEN);
        }

        // refresh 를 통한 username, role 체크
        String role = jwtUtils.getRole(refreshToken);
        String username = jwtUtils.getUserName(refreshToken);

        // 기존 refresh 토큰이 redis에 저장된것과 일치한 지 체크
        String savedToken = redisService.getData("RT:" + username);
        if (Objects.isNull(savedToken) || !savedToken.equalsIgnoreCase(refreshToken)) {
            throw new BusinessException(UserErrorCode.INVALID_TOKEN);
        }

        // access 및 refresh 새로 발금
        String access = jwtUtils.createAccessToken(username, role);
        String refresh = jwtUtils.createRefreshToken(username, role);

        // 기존 redis에 있던 refresh 삭제 및 새로운 refresh 등록
        redisService.setData("RT:" + username, refresh, Duration.ofMillis(jwtUtils.getRefreshTokenExpirationTime()));

        return TokenInfo.create("Bearer", access, refresh);
    }

    @Override
    @Transactional
    public TokenInfo issueTokenAndSaveToken(String username, String role) {
        String access = jwtUtils.createAccessToken(username, role);
        String refresh = jwtUtils.createRefreshToken(username, role);

        redisService.setData("RT:" + username, refresh, Duration.ofMillis(jwtUtils.getRefreshTokenExpirationTime()));
        return TokenInfo.create("Bearer", access, refresh);
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {

        if (Objects.nonNull(accessToken)) {
            try {
                long remaining = jwtUtils.getRemainingExpiry(accessToken);
                if (remaining > 0) {
                    redisService.setData("BL:" + accessToken, "logout", Duration.ofMillis(remaining));
                }
            } catch (Exception e) {
                log.warn("access token 블랙리스트 처리 실패: {}", e.getMessage());
            }
        }

        try {
            String username = jwtUtils.getUserName(refreshToken);
            redisService.deleteData("RT:" + username);
        } catch (Exception e) {
            log.warn("로그아웃 도중 토큰 파싱 실패 : {}", e.getMessage());
        }
    }
}
