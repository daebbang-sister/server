package com.daebbang.daebbangapi.dto.request.user;

import com.daebbang.daebbangcore.domain.command.user.UserJoinCommand;

public record JoinRequest(
    String name,
    String loginId,
    String password,
    String phoneNumber,
    String email
    //TODO : Address 추가 예정
) {
    public static UserJoinCommand toCommand(JoinRequest joinRequest) {
        return UserJoinCommand.builder()
            .name(joinRequest.name)
            .loginId(joinRequest.loginId)
            .password(joinRequest.password)
            .phoneNumber(joinRequest.phoneNumber)
            .email(joinRequest.email)
            .build();
    }
}
