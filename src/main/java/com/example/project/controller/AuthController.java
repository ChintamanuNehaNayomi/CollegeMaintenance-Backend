
package com.example.project.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.project.entity.User;
import com.example.project.service.AuthService;
import com.example.project.service.JwtService;

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody User user) {

        String result = authService.register(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", result);

        if ("Registration successful".equals(result)) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String username,
            @RequestParam String password) {

        User user = authService.login(username, password);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid username or password");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(error);
        }

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole());

        String refreshToken = jwtService.generateRefreshToken(
                user.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> currentUser(
            Authentication authentication) {
        User user = authService.findByUsername(authentication.getName());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                new AuthenticatedUserResponse(
                        user.getUsername(), user.getRole()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(
            @RequestBody RefreshRequest request) {
        String refreshToken = request == null ? null : request.refreshToken();
        Claims claims = jwtService.parseRefreshToken(refreshToken);
        if (claims == null) {
            return unauthorizedRefresh();
        }

        User user = authService.findByUsername(claims.getSubject());
        if (user == null) {
            return unauthorizedRefresh();
        }

        Map<String, String> response = new HashMap<>();
        response.put("accessToken", jwtService.generateAccessToken(
                user.getUsername(), user.getRole()));
        response.put("refreshToken", refreshToken);
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, String>> unauthorizedRefresh() {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Invalid or expired refresh token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record AuthenticatedUserResponse(String username, String role) {
    }
}
