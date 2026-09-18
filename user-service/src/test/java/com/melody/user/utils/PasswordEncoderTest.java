package com.melody.user.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {

    private final PasswordEncoder encoder = new PasswordEncoder();

    @Test
    @DisplayName("BCrypt 加密后的密文可以校验通过")
    void encodedPasswordMatches() {
        String raw = "Melody@2026";
        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoded.startsWith("$2"));
        assertTrue(encoder.matches(raw, encoded));
    }

    @Test
    @DisplayName("相同明文两次加密产生不同密文（随机盐）")
    void samePlainPasswordProducesDifferentHashes() {
        String raw = "Melody@2026";

        assertNotEquals(encoder.encode(raw), encoder.encode(raw));
    }

    @Test
    @DisplayName("错误密码校验失败")
    void wrongPasswordDoesNotMatch() {
        String encoded = encoder.encode("Melody@2026");

        assertFalse(encoder.matches("melody@2026", encoded));
        assertFalse(encoder.matches("", encoded));
    }
}
