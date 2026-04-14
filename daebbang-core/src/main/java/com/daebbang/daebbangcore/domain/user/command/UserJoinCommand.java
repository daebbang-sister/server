package com.daebbang.daebbangcore.domain.user.command;

import com.daebbang.daebbangcore.domain.address.command.AddressCommand;
import com.daebbang.daebbangcore.domain.user.entity.Users;
import lombok.Builder;

@Builder
public record UserJoinCommand(
    String name,
    String loginId,
    String password,
    String phoneNumber,
    String email,
    AddressCommand address
) {
    public static Users toEntity(UserJoinCommand command, String encodePassword) {
        return Users.createLocalUser(
            command.loginId,
            encodePassword,
            command.name,
            command.email,
            command.phoneNumber
        );
    }
}
