package com.daebbang.daebbangapi.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordResetRequest(

    @NotBlank(message = "인증 토큰은 필수 입력 값입니다.")
    String resetToken,

    @NotBlank(message = "새로운 비밀번호는 필수 입력 값입닏다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d|.*[^a-zA-Z0-9]).{8,16}$|^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,16}$",
        message = "비밀번호는 8~16자이며, 영문/숫자/특수문자 중 2가지 이상을 조합해야 합니다."
    )
    String newPassword
) {

}
