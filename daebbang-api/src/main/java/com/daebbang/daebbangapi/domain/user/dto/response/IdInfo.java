package com.daebbang.daebbangapi.domain.user.dto.response;

import com.daebbang.daebbangcommon.util.MaskingUtils;
import com.daebbang.daebbangcore.domain.user.entity.Provider;

public record IdInfo(
    String id,
    Provider provider
) {
    public static IdInfo ofMasked(String id, Provider provider) {
        return new IdInfo(MaskingUtils.maskUserId(id), provider);
    }
}
