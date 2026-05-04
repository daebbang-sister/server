package com.daebbang.daebbangcommon.util;

import com.daebbang.daebbangcommon.error.BusinessException;
import com.daebbang.daebbangcommon.error.CommonErrorCode;

public final class PasswordPolicy {

    public static final String PATTERN =
        "^(?=.*[a-zA-Z])(?=.*\\d|.*[^a-zA-Z0-9]).{8,16}$|^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,16}$";

    public static final String MESSAGE =
        "비밀번호는 8~16자이며, 영문/숫자/특수문자 중 2가지 이상을 조합해야 합니다.";

    private PasswordPolicy() {
        throw new BusinessException(CommonErrorCode.CANNOT_INSTANTIATE_UTIL_CLASS);
    }
}
