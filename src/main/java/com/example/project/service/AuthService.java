
package com.example.project.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.entity.User;
import com.example.project.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(User user) {

        if (user.getUsername() == null ||
                user.getUsername().isBlank()) {
            return "Username is required";
        }

        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {
            return "Password is required";
        }

        if (userRepository.findByUsernameIgnoreCase(user.getUsername().trim())
                .isPresent()) {
            return "Username already exists";
        }

        if (user.getEmail() != null &&
                !user.getEmail().isBlank()) {
            if (userRepository.findByEmail(user.getEmail().trim())
                    .isPresent()) {
                return "Email already exists";
            }
            user.setEmail(user.getEmail().trim());
        }

        user.setUsername(user.getUsername().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Public registrations must always be students.
        user.setRole("STUDENT");

        userRepository.save(user);

        return "Registration successful";
    }

    public User login(String username, String password) {

        if (username == null || password == null) {
            return null;
        }

        User user = userRepository
                .findByUsernameIgnoreCase(username.trim())
                .orElse(null);

        if (user != null &&
                passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    public User findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return userRepository.findByUsernameIgnoreCase(username.trim())
                .orElse(null);
    }

    public User createStaffAccount(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Username is required");
        }
        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters long");
        }

        String normalizedUsername = username.trim();
        if (userRepository.findByUsernameIgnoreCase(normalizedUsername)
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Username already exists");
        }

        User staff = new User();
        staff.setUsername(normalizedUsername);
        staff.setPassword(passwordEncoder.encode(password));
        staff.setRole("STAFF");
        return userRepository.save(staff);
    }

    public List<User> getStaffAccounts() {
        return userRepository.findByRole("STAFF");
    }
}
