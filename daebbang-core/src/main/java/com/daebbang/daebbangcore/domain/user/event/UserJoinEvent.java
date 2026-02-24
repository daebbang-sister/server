package com.daebbang.daebbangcore.domain.user.event;

public record UserJoinEvent(
    String phoneNumber
) {
    public static UserJoinEvent toEvent(String phoneNumber) {
        return new UserJoinEvent(phoneNumber);
    }
}
