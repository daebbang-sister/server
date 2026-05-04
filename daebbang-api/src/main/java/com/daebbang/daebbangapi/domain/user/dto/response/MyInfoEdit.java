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
    /**
     * Create a MyInfoEdit DTO populated from the given Users entity.
     *
     * @param user the source Users entity whose id, provider, loginId, name, email and phone number are mapped into the DTO
     * @return a MyInfoEdit instance containing values copied from the provided user
     */
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
