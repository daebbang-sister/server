package com.daebbang.daebbangapi.domain.address.dto.request;

import com.daebbang.daebbangcommon.util.PhoneNumberPolicy;
import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddressSaveRequest(

    @NotBlank(message = "수령인 이름은 필수 입력값입니다.")
    String receiver,

    @NotBlank(message = "수령인 전화번호는 필수 입력값입니다.")
    @Pattern(
        regexp = PhoneNumberPolicy.PATTERN,
        message = PhoneNumberPolicy.MESSAGE
    )
    String receiverPhoneNumber,

    @Nullable
    String alias,

    @NotBlank(message = "우편번호는 필수 입력값입니다.")
    String zipCode,

    @NotBlank(message = "도로명 주소는 필수 입력값입니다.")
    String address,

    @NotBlank(message = "상세 주소는 필수 입력값입니다.")
    String detailAddress,

    boolean isDefault
) {
    public AddressCommand toCommand() {
        return new AddressCommand(receiver, receiverPhoneNumber, alias, zipCode, address, detailAddress, isDefault);
    }
}
