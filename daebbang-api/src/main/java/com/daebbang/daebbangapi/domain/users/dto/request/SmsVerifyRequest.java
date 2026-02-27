package com.daebbang.daebbangapi.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsVerifyRequest(

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(
        regexp = "^010-\\d{3,4}-\\d{4}$",
        message = "전화번호 형식(010-XXXX-XXXX)이 올바르지 않습니다."
    )
    String phoneNumber,

    @NotBlank(message = "인증 번호는 필수 입력값입니다.")
    String authCode
) {

}
