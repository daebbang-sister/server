package com.daebbang.daebbangcommon.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;

public final class PhoneNumberPolicy {

    public static final String PATTERN = "^010-\\d{3,4}-\\d{4}$";

    public static final String MESSAGE = "전화번호 형식(010-XXXX-XXXX)이 올바르지 않습니다.";

    /**
     * Prevents instantiation of this utility class by throwing a BusinessException.
     *
     * @throws BusinessException always thrown with CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS when construction is attempted
     */
    private PhoneNumberPolicy() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }
}
