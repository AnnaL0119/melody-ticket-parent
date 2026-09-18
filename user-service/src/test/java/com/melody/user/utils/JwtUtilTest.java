package com.melody.user.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-key-please-rotate-0123456789abcdef";
    private static final long ONE_HOUR = 3600_000L;

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", ONE_HOUR);
    }

    @Test
    @DisplayName("生成的 Token 可以还原出 userId 与 username")
    void generateTokenCanBeParsed() {
        String token = jwtUtil.generateToken(42L, "melody");

        assertNotNull(token);
        assertEquals(42L, jwtUtil.getUserIdFromToken(token));
        assertEquals("melody", jwtUtil.getUsernameFromToken(token));
        assertTrue(jwtUtil.validateToken(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("使用其他密钥签名的 Token 校验失败")
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "secret", "another-secret-key-0123456789abcdefghij");
        ReflectionTestUtils.setField(other, "expiration", ONE_HOUR);

        String token = other.generateToken(1L, "hacker");

        assertFalse(jwtUtil.validateToken(token));
        assertThrows(Exception.class, () -> jwtUtil.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("被篡改的 Token 校验失败")
    void tamperedTokenIsRejected() {
        String token = jwtUtil.generateToken(7L, "alice");
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".AAAA" + parts[2].substring(4);

        assertFalse(jwtUtil.validateToken(tampered));
    }

    @Test
    @DisplayName("已过期的 Token 校验失败且被标记为过期")
    void expiredTokenIsRejected() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);

        String token = jwtUtil.generateToken(9L, "bob");

        assertFalse(jwtUtil.validateToken(token));
        assertTrue(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("非法格式的 Token 校验失败而不是抛出异常")
    void malformedTokenReturnsFalse() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }
}
