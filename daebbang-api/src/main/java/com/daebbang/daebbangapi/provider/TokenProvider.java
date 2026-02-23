package com.daebbang.daebbangapi.provider;

import com.daebbang.daebbangcommon.dto.authority.TokenInfo;
import com.daebbang.daebbangcommon.dto.response.CommonResponse;
import com.daebbang.daebbangcommon.success.UserSuccessCode;
import com.daebbang.daebbangcore.infra.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenProvider {

    private final JwtUtils jwtUtils;

    public CommonResponse<TokenInfo> issueTokens(UserDetails user) {
        String userRole = user.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority();
        return issueTokens(user.getUsername(), userRole);
    }

    public CommonResponse<TokenInfo> issueTokens(String username, String role) {
        String accessToken = jwtUtils.createAccessToken(username, role);
        String refreshToken = jwtUtils.createRefreshToken(username, role);

        return CommonResponse.success(
            UserSuccessCode.USER_LOGIN,
            TokenInfo.create(accessToken, refreshToken)
        );
    }
}
