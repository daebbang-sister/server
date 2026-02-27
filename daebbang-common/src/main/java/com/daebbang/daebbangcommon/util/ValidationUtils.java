package com.daebbang.daebbangcommon.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import java.util.Objects;

public class ValidationUtils {
    private ValidationUtils() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }

    public static void validateLoginRequest(String id, String password) {
        if (Objects.isNull(id) || id.length() < 4 || id.length() > 16) {
            throw new BusinessException(UserErrorCode.INVALID_USER_ID_FORMAT);
        }

        if (!isValidPassword(password)) {
            throw new BusinessException(UserErrorCode.INVALID_USER_PASSWORD_FORMAT);
        }
    }

    private static boolean isValidPassword(String password) {
        if (Objects.isNull(password) || password.length() < 8 || password.length() > 16) return false;

        int matches = 0;
        if (password.matches(".*[a-zA-Z].*")) matches++;
        if (password.matches(".*\\d.*")) matches++;
        if (password.matches(".*[^a-zA-Z0-9].*")) matches++;

        return matches >= 2;
    }
}
