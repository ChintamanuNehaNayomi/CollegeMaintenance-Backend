
package com.example.project.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.repository.MaintenanceRequestRepository;
import com.example.project.repository.UserRepository;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;
    private final UserRepository userRepository;

    public MaintenanceRequestService(
            MaintenanceRequestRepository repository,
            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public MaintenanceRequest createRequest(MaintenanceRequest request) {
    request.setStatus("PENDING");
    request.setApprovedBy(null);
    request.setAssignedTo(null);
    request.setRequestDate(LocalDate.now());
    return repository.save(request);
}

    public List<MaintenanceRequest> getRequestsByUsername(
            String username) {
        return repository.findByUsername(username);
    }

    public List<MaintenanceRequest> getAllRequests() {
        return repository.findAll();
    }

    public List<MaintenanceRequest> getRequestsByStatus(
            String status) {
        return repository.findByStatus(status);
    }

    public MaintenanceRequest updateStatus(
            Long id, String status, String adminUsername) {

        MaintenanceRequest request = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        request.setStatus(status);
        request.setApprovedBy(adminUsername);

        return repository.save(request);
    }

    public MaintenanceRequest assignStaff(
            Long id, String staffUsername) {

        MaintenanceRequest request = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        if (staffUsername == null || staffUsername.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A staff username is required");
        }

        boolean isStaffAccount = userRepository
                .findByUsernameIgnoreCase(staffUsername.trim())
                .filter(user -> "STAFF".equals(user.getRole()))
                .isPresent();

        if (!isStaffAccount) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The username does not belong to a staff account");
        }

        request.setAssignedTo(staffUsername.trim());

        return repository.save(request);
    }

    public List<MaintenanceRequest> getAssignedRequests(
            String staffUsername) {
        return repository.findByAssignedTo(staffUsername);
            }
        
    public MaintenanceRequest updateStaffStatus(
            Long id, String status, String staffUsername) {

        MaintenanceRequest request = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Request not found"));

        if (!staffUsername.equals(request.getAssignedTo())) {
            throw new RuntimeException(
                    "You are not assigned to this request");
        }

        if (!"IN_PROGRESS".equals(status)
                && !"COMPLETED".equals(status)) {
            throw new IllegalArgumentException(
                    "Status must be IN_PROGRESS or COMPLETED");
        }

        request.setStatus(status);
        return repository.save(request);
    

    }
}
