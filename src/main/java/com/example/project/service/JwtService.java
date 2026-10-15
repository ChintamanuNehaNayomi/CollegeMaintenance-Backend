package com.example.project.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final long ACCESS_TOKEN_TTL_MILLIS = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_TTL_MILLIS =
            7L * 24 * 60 * 60 * 1000;
    private static final Set<String> VALID_ROLES =
            Set.of("STUDENT", "STAFF", "ADMIN");

    private final SecretKey key;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        if (secret == null
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "JWT_SECRET must be configured with at least 32 UTF-8 bytes");
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("token_type", "access")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + ACCESS_TOKEN_TTL_MILLIS))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("token_type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + REFRESH_TOKEN_TTL_MILLIS))
                .signWith(key)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null
                || !"access".equals(claims.get("token_type", String.class))
                || claims.getSubject() == null
                || claims.getSubject().isBlank()) {
            return null;
        }

        String role = claims.get("role", String.class);
        return VALID_ROLES.contains(role) ? claims : null;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null
                || !"refresh".equals(claims.get("token_type", String.class))
                || claims.getSubject() == null
                || claims.getSubject().isBlank()) {
            return null;
        }
        return claims;
    }

    private Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }
}
