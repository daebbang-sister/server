package com.daebbang.daebbangapi.domain.users.dto.request;

public record UserPasswordFindRequest(
    String username,
    String userId,
    String userEmail
) {

}
