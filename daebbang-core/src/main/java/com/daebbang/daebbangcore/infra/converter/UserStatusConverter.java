package com.daebbang.daebbangcore.infra.converter;

import com.daebbang.daebbangcore.domain.user.entity.UserStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter extends AbstractBaseEnumConverter<UserStatus, Integer> {
    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
