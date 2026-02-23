package com.daebbang.daebbangcore.domain.user.command;

public interface PasswordPort {
    String encode(String rawPassword);
    boolean matches(String raw, String encoded);
}
