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
    /**
     * Indicates whether a password update value was provided.
     *
     * @return `true` if `password` is non-null and contains at least one non-whitespace character, `false` otherwise.
     */
    public boolean hasPassword() {
        return password != null && !password.isBlank();
    }

    /**
     * Determines whether a phone number value is present and contains non-whitespace characters.
     *
     * @return true if {@code phoneNumber} is non-null and not blank, false otherwise.
     */
    public boolean hasPhoneNumber() {
        return phoneNumber != null && !phoneNumber.isBlank();
    }

    /**
     * Indicates whether an email update value is present.
     *
     * @return true if the email field is non-null and contains non-whitespace characters, false otherwise.
     */
    public boolean hasEmail() {
        return email != null && !email.isBlank();
    }
}
