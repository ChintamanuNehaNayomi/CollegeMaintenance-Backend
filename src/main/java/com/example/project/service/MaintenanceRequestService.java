package com.example.project.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.project.entity.MaintenanceRequest;
import com.example.project.repository.MaintenanceRequestRepository;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;

    public MaintenanceRequestService(
            MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    public MaintenanceRequest createRequest(
            MaintenanceRequest request) {

        request.setStatus("SUBMITTED");
        request.setRequestDate(LocalDate.now());

        return repository.save(request);
    }

    public List<MaintenanceRequest> getRequestsByUsername(
            String username) {

        return repository.findByUsername(username);
    }
}
