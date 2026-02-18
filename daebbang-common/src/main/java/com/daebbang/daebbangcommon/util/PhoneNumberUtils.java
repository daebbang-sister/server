package com.daebbang.daebbangcommon.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;
import com.daebbang.daebbangcommon.error.UserErrorCode;
import java.util.Objects;

public class PhoneNumberUtils {

    private PhoneNumberUtils() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }

    public static String parsePhoneNumber(String phoneNumber) {
        if (Objects.isNull(phoneNumber)) {
            throw new BusinessException(UserErrorCode.INVALID_PHONE_NUMBER_FORMAT);
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    public static String maskPhoneNumber(String phoneNumber) {
        if (Objects.isNull(phoneNumber) || phoneNumber.length() < 11) {
            throw new BusinessException(UserErrorCode.INVALID_PHONE_NUMBER_FORMAT);
        }
        return phoneNumber.replaceAll("(\\d{3})-\\d{4}-(\\d{4})", "$1-****-$2");
    }
}
