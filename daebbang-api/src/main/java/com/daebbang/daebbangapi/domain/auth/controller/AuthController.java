package com.daebbang.daebbangapi.domain.auth.controller;

import com.daebbang.daebbangapi.utils.CookieUtils;
import com.daebbang.daebbangcommon.dto.authority.TokenInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.CommonSuccessCode;
import com.daebbang.daebbangcore.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tokens")
public class AuthController {

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    @PostMapping("/reissues")
    public ResponseEntity<@NonNull CommonResponse<TokenInfo>> reissueTokens(@CookieValue(name = "refresh") String refreshToken,
        HttpServletResponse response) {

        TokenInfo info = authService.rotateRefreshToken(refreshToken);

        cookieUtils.addRefreshCookie(response, info.refreshToken());

        return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(CommonResponse.success(
                                CommonSuccessCode.CREATE_SUCCESS,
                                TokenInfo.create(info.grantType(), info.accessToken()))
                            );
    }
}