package com.daebbang.daebbangapi.domain.user.dto.request;

import com.daebbang.daebbangapi.domain.user.dto.vo.AddressVO;
import com.daebbang.daebbangcommon.util.PasswordPolicy;
import com.daebbang.daebbangcommon.util.PhoneNumberPolicy;
import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import com.daebbang.daebbangcore.domain.user.command.UserJoinCommand;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record JoinRequest(

    @NotBlank(message = "회원 이름은 필수 입력값입니다.")
    String name,

    @NotBlank(message = "아이디는 비어있어서는 안됩니다.")
    @Size(min = 4, max = 16, message = "아이디는 4자에서 16자 사이여야 합니다.")
    @Pattern(
        regexp = "^[a-z0-9]+$",
        message = "아이디는 영문 소문자와 숫자만 입력 가능하며, 특수문자는 사용할 수 없습니다."
    )
    String loginId,

    @NotBlank(message = "비밀번호는 비어있어서는 안됩니다.")
    @Pattern(
        regexp = PasswordPolicy.PATTERN,
        message = PasswordPolicy.MESSAGE
    )
    String password,

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(
        regexp = PhoneNumberPolicy.PATTERN,
        message = PhoneNumberPolicy.MESSAGE
    )
    String phoneNumber,

    @Email(message = "올바른 이메일 형식을 입력해야 합니다.")
    String email,

    @Nullable
    AddressVO address
) {
    public static UserJoinCommand toCommand(JoinRequest joinRequest) {
        AddressVO addr = joinRequest.address();
        AddressCommand addressCommand = addr != null
            ? new AddressCommand(joinRequest.name, joinRequest.phoneNumber,
                addr.alias(), addr.zipCode(), addr.address(), addr.detailAddress(), true)
            : null;
        return UserJoinCommand.builder()
            .name(joinRequest.name)
            .loginId(joinRequest.loginId)
            .password(joinRequest.password)
            .phoneNumber(joinRequest.phoneNumber)
            .email(joinRequest.email)
            .address(addressCommand)
            .build();
    }
}
