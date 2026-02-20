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

        String username = user.getUsername();
        String userRole = user.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtUtils.createAccessToken(username, userRole);
        String refreshToken = jwtUtils.createRefreshToken(username, userRole);

        return CommonResponse.success(
            UserSuccessCode.USER_LOGIN,
            TokenInfo.create(accessToken, refreshToken)
        );
    }
}
