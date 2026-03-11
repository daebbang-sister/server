package com.daebbang.daebbangcore.domain.auth.service;

import com.daebbang.daebbangcommon.dto.authority.TokenInfo;

public interface AuthService {
    TokenInfo rotateRefreshToken(String refreshToken);
    TokenInfo issueTokenAndSaveToken(String username, String role);
}
