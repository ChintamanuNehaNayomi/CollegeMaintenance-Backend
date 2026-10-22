
package com.example.project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.entity.User;
import com.example.project.service.AuthService;
import com.example.project.service.MaintenanceRequestService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MaintenanceRequestService service;
    private final AuthService authService;

    public AdminController(
            MaintenanceRequestService service,
            AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping("/requests")
    public List<MaintenanceRequest> getAllRequests() {
        return service.getAllRequests();
    }

    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<MaintenanceRequest> approveRequest(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.updateStatus(
                        id, "APPROVED", authentication.getName()));
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<MaintenanceRequest> rejectRequest(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                service.updateStatus(
                        id, "REJECTED", authentication.getName()));
    }

    @PutMapping("/requests/{id}/assign")
    public ResponseEntity<MaintenanceRequest> assignStaff(
            @PathVariable Long id,
            @RequestParam String staffUsername) {

        return ResponseEntity.ok(
                service.assignStaff(id, staffUsername));
    }

    // Create a staff account (ADMIN only)
    @PostMapping("/staff")
    public ResponseEntity<Map<String, String>> createStaff(
            @RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        User staff = authService.createStaffAccount(username, password);

        return ResponseEntity.ok(Map.of(
                "message", "Staff account created successfully",
                "username", staff.getUsername(),
                "role", staff.getRole()));
    }

    // List staff accounts (ADMIN only)
    @GetMapping("/staff")
    public List<Map<String, String>> getStaffAccounts() {
        return authService.getStaffAccounts().stream()
                .map(staff -> Map.of(
                        "username", staff.getUsername(),
                        "role", staff.getRole()))
                .toList();
    }
}
