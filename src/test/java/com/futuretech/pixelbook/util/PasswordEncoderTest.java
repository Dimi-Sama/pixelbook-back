package com.futuretech.pixelbook.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new PasswordEncoder();
    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    @Test
    void testEncode() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(bcryptEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testMatchesWithValidPassword() {
        String rawPassword = "testPassword123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testMatchesWithInvalidPassword() {
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        assertFalse(passwordEncoder.matches(wrongPassword, encodedPassword));
    }

    @Test
    void testEncodeNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.encode(null);
        });
    }

    @Test
    void testMatchesWithNullPassword() {
        String encodedPassword = passwordEncoder.encode("test");
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.matches(null, encodedPassword);
        });
    }

    @Test
    void testMatchesWithNullEncodedPassword() {
        assertThrows(IllegalArgumentException.class, () -> {
            passwordEncoder.matches("test", null);
        });
    }
} 