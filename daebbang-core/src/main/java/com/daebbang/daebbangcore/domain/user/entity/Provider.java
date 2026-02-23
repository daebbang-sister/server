package com.daebbang.daebbangcore.domain.user.entity;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Provider {
    LOCAL("local"),
    KAKAO("kakao");

    private final String registrationId;

    Provider(String registrationId) {
        this.registrationId = registrationId;
    }

    public static Provider findByRegistrationId(String id) {
        return Arrays.stream(values())
                    .filter(p -> p.registrationId.equalsIgnoreCase(id))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(UserErrorCode.UNSUPPORTED_SOCIAL_PROVIDER));
    }
}
