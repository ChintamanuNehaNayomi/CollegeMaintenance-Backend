package com.example.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;

class AuthServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void publicRegistrationAlwaysCreatesStudentWithEncodedPassword() {
        User submitted = new User();
        submitted.setUsername(" newstudent ");
        submitted.setPassword("student-pass");
        submitted.setRole("ADMIN");
        when(userRepository.findByUsernameIgnoreCase("newstudent"))
                .thenReturn(Optional.empty());

        assertEquals("Registration successful", authService.register(submitted));

        assertEquals("STUDENT", submitted.getRole());
        assertNotEquals("student-pass", submitted.getPassword());
        assertTrue(passwordEncoder.matches("student-pass", submitted.getPassword()));
        verify(userRepository).save(submitted);
        when(userRepository.findByUsernameIgnoreCase("newstudent"))
                .thenReturn(Optional.of(submitted));
        assertEquals("STUDENT",
                authService.login("newstudent", "student-pass").getRole());
    }

    @Test
    void adminCreatedStaffAccountUsesStaffRoleAndCanAuthenticate() {
        when(userRepository.findByUsernameIgnoreCase("staffuser"))
                .thenReturn(Optional.empty());

        User staff = authService.createStaffAccount(
                " staffuser ", "staff-pass-123");
        when(userRepository.findByUsernameIgnoreCase("staffuser"))
                .thenReturn(Optional.of(staff));

        assertEquals("STAFF", staff.getRole());
        assertNotEquals("staff-pass-123", staff.getPassword());
        assertTrue(passwordEncoder.matches("staff-pass-123", staff.getPassword()));
        assertNotNull(authService.login("staffuser", "staff-pass-123"));
        assertNull(authService.login("staffuser", "wrong-password"));
    }

    @Test
    void existingAdminAccountCanAuthenticate() {
        User admin = new User();
        admin.setUsername("adminuser");
        admin.setPassword(passwordEncoder.encode("admin-pass-123"));
        admin.setRole("ADMIN");
        when(userRepository.findByUsernameIgnoreCase("adminuser"))
                .thenReturn(Optional.of(admin));

        User authenticated = authService.login("adminuser", "admin-pass-123");

        assertNotNull(authenticated);
        assertEquals("ADMIN", authenticated.getRole());
    }

    @Test
    void staffCreationRejectsDuplicateUsernameAndShortPassword() {
        User existing = new User();
        existing.setUsername("staffuser");
        when(userRepository.findByUsernameIgnoreCase("staffuser"))
                .thenReturn(Optional.of(existing));

        org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> authService.createStaffAccount("staffuser", "long-password"));
        org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> authService.createStaffAccount("another", "short"));
    }
}
