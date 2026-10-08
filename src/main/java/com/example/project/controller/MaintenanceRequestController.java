package com.example.project.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.service.MaintenanceRequestService;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002"
})
public class MaintenanceRequestController {

    private final MaintenanceRequestService service;

    public MaintenanceRequestController(
            MaintenanceRequestService service) {
        this.service = service;
    }

    @PostMapping
    public MaintenanceRequest createRequest(
            @RequestBody MaintenanceRequest request) {

        return service.createRequest(request);
    }

    @GetMapping("/{username}")
    public List<MaintenanceRequest> getRequests(
            @PathVariable String username) {

        return service.getRequestsByUsername(username);
    }
}
