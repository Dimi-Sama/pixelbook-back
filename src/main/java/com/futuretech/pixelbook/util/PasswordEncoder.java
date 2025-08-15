package com.futuretech.pixelbook.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return encoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Raw password cannot be null");
        }
        if (encodedPassword == null) {
            throw new IllegalArgumentException("Encoded password cannot be null");
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
} 