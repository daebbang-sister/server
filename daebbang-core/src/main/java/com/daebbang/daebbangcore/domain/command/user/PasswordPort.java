package com.daebbang.daebbangcore.domain.command.user;

public interface PasswordPort {
    String encode(String rawPassword);
    boolean matches(String raw, String encoded);
}
