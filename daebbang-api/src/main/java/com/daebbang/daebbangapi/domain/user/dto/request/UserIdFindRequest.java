package com.daebbang.daebbangapi.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserIdFindRequest(

    @NotBlank(message = "회원 이름은 필수 입력값입니다.")
    String username,

    @Email(message = "올바른 이메일 형식을 입력해야 합니다.")
    String userEmail
) {

}