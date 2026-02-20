package com.daebbang.daebbangcore.command.user;

public interface PasswordPort {
    String encode(String rawPassword);
    boolean matches(String raw, String encoded);
}
