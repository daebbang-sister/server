package com.daebbang.daebbangapi.dto.response.user;

import com.daebbang.daebbangcommon.util.PhoneNumberUtils;
import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record UserInfo(
    Long id,
    Provider provider,
    String loginId,
    String userName,
    String userPhoneNumber,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt
) {
    public static UserInfo from(Users user) {
        return UserInfo.builder()
            .id(user.getId())
            .provider(user.getProvider())
            .loginId(user.getLoginId())
            .userName(user.getName())
            .userPhoneNumber(PhoneNumberUtils.maskPhoneNumber(user.getPhoneNumber()))
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
