package com.daebbang.daebbangapi.domain.user.dto.vo;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record AddressVO(

    @Nullable
    String alias,

    @NotBlank(message = "우편 번호는 비어있어서는 안됩니다.")
    String zipCode,

    @NotBlank(message = "도로명 주소는 비어있어서는 안됩니다.")
    String address,

    @NotBlank(message = "상세 주소는 비어있어서는 안됩니다.")
    String detailAddress
) {

}
