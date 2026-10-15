
package com.example.project.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.service.MaintenanceRequestService;

@RestController
@RequestMapping("/api/requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    public MaintenanceRequestController(
            MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceRequest> createRequest(
            @RequestBody MaintenanceRequest request,
            Authentication authentication) {

        request.setUsername(authentication.getName());

        MaintenanceRequest savedRequest =
                maintenanceRequestService.createRequest(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
    }

    @GetMapping("/{username}")
    public ResponseEntity<List<MaintenanceRequest>> getRequests(
            @PathVariable String username,
            Authentication authentication) {

        if (!authentication.getName().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<MaintenanceRequest> requests =
                maintenanceRequestService.getRequestsByUsername(
                        authentication.getName());

        return ResponseEntity.ok(requests);
    }
}
