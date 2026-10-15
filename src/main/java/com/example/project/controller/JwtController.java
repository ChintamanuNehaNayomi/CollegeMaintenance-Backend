package com.example.project.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.entity.User;
import com.example.project.service.AuthService;
import com.example.project.service.JwtService;

@RestController
public class JwtController {

    private final AuthService authService;
    private final JwtService jwtService;

    public JwtController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/login")
    public Map<String, String> login(
            @RequestParam String username,
            @RequestParam String password) {

        User user = authService.login(username, password);

        if (user == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid username or password");
            return error;
        }

        String accessToken =
                jwtService.generateAccessToken(
                        user.getUsername(),
                        user.getRole()
                );

        String refreshToken =
                jwtService.generateRefreshToken(
                        user.getUsername()
                );

        Map<String, String> response = new HashMap<>();

        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return response;
    }
}
