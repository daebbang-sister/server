package com.daebbang.daebbangapi.domain.user.dto.request;

import com.daebbang.daebbangcommon.util.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(

    @NotBlank(message = "인증 토큰은 필수 입력 값입니다.")
    String resetToken,

    @NotBlank(message = "새로운 비밀번호는 필수 입력 값입닏다.")
    @Pattern(
        regexp = PasswordPolicy.PATTERN,
        message = PasswordPolicy.MESSAGE
    )
    String newPassword
) {

}
