package com.daebbang.daebbangapi.domain.user.dto.response;

import com.daebbang.daebbangcore.domain.user.entity.Provider;
import com.daebbang.daebbangcore.domain.user.entity.Users;

public record MyInfoEdit(
    Long id,
    Provider provider,
    String loginId,
    String userName,
    String userEmail,
    String userPhoneNumber
) {
    public static MyInfoEdit from(Users user) {
        return new MyInfoEdit(
            user.getId(),
            user.getProvider(),
            user.getLoginId(),
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber()
        );
    }
}
