package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String TEST_SECRET =
            "unit-test-signing-secret-with-at-least-32-bytes";

    @Test
    void accessAndRefreshTokensCannotBeUsedInterchangeably() {
        JwtService jwtService = new JwtService(TEST_SECRET);
        String accessToken = jwtService.generateAccessToken(
                "student1", "STUDENT");
        String refreshToken = jwtService.generateRefreshToken("student1");

        assertNotNull(jwtService.parseAccessToken(accessToken));
        assertNull(jwtService.parseRefreshToken(accessToken));
        assertNotNull(jwtService.parseRefreshToken(refreshToken));
        assertNull(jwtService.parseAccessToken(refreshToken));
    }

    @Test
    void signingSecretMustHaveAtLeastThirtyTwoBytes() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService("too-short"));
        assertTrue(exception.getMessage().contains(
                "JWT_SECRET must be configured with at least 32 UTF-8 bytes"));
    }

    @Test
    void signingSecretCannotBeBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtService(""));
        assertTrue(exception.getMessage().contains(
                "JWT_SECRET must be configured with at least 32 UTF-8 bytes"));
    }
}
