package com.example.project.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.entity.User;
import com.example.project.service.AuthService;

@RestController
@RequestMapping("/api/admin/staff")
public class AdminStaffController {

    private final AuthService authService;

    public AdminStaffController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public List<StaffAccountResponse> getStaffAccounts() {
        return authService.getStaffAccounts()
                .stream()
                .map(user -> new StaffAccountResponse(
                        user.getUsername(), user.getRole()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<StaffAccountResponse> createStaffAccount(
            @RequestBody StaffAccountRequest request) {
        User staff = authService.createStaffAccount(
                request.username(), request.password());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StaffAccountResponse(
                        staff.getUsername(), staff.getRole()));
    }

    public record StaffAccountRequest(String username, String password) {
    }

    public record StaffAccountResponse(String username, String role) {
    }
}
