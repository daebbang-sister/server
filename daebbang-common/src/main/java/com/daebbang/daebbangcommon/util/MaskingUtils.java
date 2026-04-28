package com.daebbang.daebbangcommon.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import java.util.Objects;

public class MaskingUtils {

    static final String FIXED_MASK = "********";

    private MaskingUtils() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }

    public static String maskUserId(String userId) {
        if (Objects.isNull(userId) || userId.length() < 4) {
            throw new BusinessException(UserErrorCode.INVALID_USER_ID_FORMAT);
        }

        return userId.substring(0, 3) + FIXED_MASK;
    }

    public static String maskReviewerId(String userId) {
        if (Objects.isNull(userId) || userId.isBlank()) {
            return FIXED_MASK;
        }
        int len = userId.length();
        int visible;
        if (len > 4) {
            visible = 4;
        } else if (len > 2) {
            visible = 2;
        } else {
            visible = 1;
        }
        return userId.substring(0, visible) + FIXED_MASK;
    }
}
