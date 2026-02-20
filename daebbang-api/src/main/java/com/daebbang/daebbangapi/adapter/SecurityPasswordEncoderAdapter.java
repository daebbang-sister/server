package com.daebbang.daebbangapi.adapter;

import com.daebbang.daebbangcore.domain.command.user.PasswordPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityPasswordEncoderAdapter implements PasswordPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}
