package com.daebbang.daebbangapi.domain.user.dto.request;

import com.daebbang.daebbangcommon.util.PhoneNumberPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsSendRequest(
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(
        regexp = PhoneNumberPolicy.PATTERN,
        message = PhoneNumberPolicy.MESSAGE
    )
    String phoneNumber
) {

}
