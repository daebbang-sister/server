package com.daebbang.daebbangcommon.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import java.util.Objects;

public class MaskingUtils {
    private MaskingUtils() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }

    public static String maskUserId(String userId) {
        if (Objects.isNull(userId) || userId.length() < 4) {
            throw new BusinessException(UserErrorCode.INVALID_USER_ID_FORMAT);
        }
        final String FIXED_MASK = "********";
        return userId.substring(0, 3) + FIXED_MASK;
    }

    public static String maskUsername(String username) {
        if (Objects.isNull(username) || username.length() < 2) {
            throw new BusinessException(UserErrorCode.INVALID_USERNAME_FORMAT);
        }

        int len = username.length();
        if (len == 2) {
            return username.charAt(0) + "*";
        }

        return username.charAt(0) + "*".repeat(len - 2) + username.substring(len - 1);
    }
}
