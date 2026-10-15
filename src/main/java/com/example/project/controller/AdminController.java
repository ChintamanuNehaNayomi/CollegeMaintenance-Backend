
package com.example.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.service.MaintenanceRequestService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MaintenanceRequestService service;

    public AdminController(MaintenanceRequestService service) {
        this.service = service;
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
            service.updateStatus(id, "APPROVED",
                authentication.getName())
        );
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<MaintenanceRequest> rejectRequest(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
            service.updateStatus(id, "REJECTED",
                authentication.getName())
        );
    }

    @PutMapping("/requests/{id}/assign")
    public ResponseEntity<MaintenanceRequest> assignStaff(
            @PathVariable Long id,
            @RequestParam String staffUsername) {

        return ResponseEntity.ok(
            service.assignStaff(id, staffUsername)
        );
    }
}
