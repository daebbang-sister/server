package com.daebbang.daebbangapi.domain.user.dto.request;

import com.daebbang.daebbangcommon.util.PasswordPolicy;
import com.daebbang.daebbangcommon.util.PhoneNumberPolicy;
import com.daebbang.daebbangcore.domain.user.command.MyInfoUpdateCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.jspecify.annotations.Nullable;

public record MyInfoUpdateRequest(

    @Nullable
    @Pattern(
        regexp = PasswordPolicy.PATTERN,
        message = PasswordPolicy.MESSAGE
    )
    String password,

    @Nullable
    String passwordConfirm,

    @Nullable
    @Pattern(
        regexp = PhoneNumberPolicy.PATTERN,
        message = PhoneNumberPolicy.MESSAGE
    )
    String phoneNumber,

    @Nullable
    @Email(message = "올바른 이메일 형식을 입력해야 합니다.")
    String email
) {
    public MyInfoUpdateCommand toCommand() {
        return MyInfoUpdateCommand.builder()
            .password(password)
            .passwordConfirm(passwordConfirm)
            .phoneNumber(phoneNumber)
            .email(email)
            .build();
    }
}
