package com.daebbang.daebbangapi.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CheckDuplicationIdRequest(
    @NotBlank(message = "아이디는 비어있어서는 안됩니다.")
    @Size(min = 4, max = 16, message = "아이디는 4자에서 16자 사이여야 합니다.")
    String loginId
) {

}
