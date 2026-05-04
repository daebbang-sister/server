package com.daebbang.daebbangcore.domain.user.command;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record MyInfoUpdateCommand(
    @Nullable String password,
    @Nullable String passwordConfirm,
    @Nullable String phoneNumber,
    @Nullable String email
) {
    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }
}
