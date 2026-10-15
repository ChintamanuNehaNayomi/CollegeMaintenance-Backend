
package com.example.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.service.MaintenanceRequestService;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final MaintenanceRequestService service;

    public StaffController(MaintenanceRequestService service) {
        this.service = service;
    }

    @GetMapping("/requests")
    public List<MaintenanceRequest> getAssignedRequests(
            Authentication authentication) {

        return service.getAssignedRequests(authentication.getName());
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<MaintenanceRequest> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        return ResponseEntity.ok(
            service.updateStaffStatus(
                id, status, authentication.getName()
            )
        );
    }
}
