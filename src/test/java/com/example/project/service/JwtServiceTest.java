package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

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

    @Test
    void expiredAndMalformedTokensAreRejected() {
        JwtService jwtService = new JwtService(TEST_SECRET);
        SecretKey key = Keys.hmacShaKeyFor(
                TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("student1")
                .claim("role", "STUDENT")
                .claim("token_type", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(key)
                .compact();

        assertNull(jwtService.parseAccessToken(expiredToken));
        assertNull(jwtService.parseAccessToken("not-a-jwt"));
        assertNull(jwtService.parseRefreshToken("not-a-jwt"));
    }
}
