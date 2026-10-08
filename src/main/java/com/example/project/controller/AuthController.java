package com.example.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.entity.User;
import com.example.project.service.AuthService;
import com.example.project.service.JwtService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @GetMapping("/")
    public String home() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {

        String result = authService.register(user);

        if (result.equals("Username already exists")) {
            model.addAttribute("error", result);
            return "register";
        }

        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = authService.login(username, password);

        if (user == null) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        // Generate JWT Access Token
        String accessToken =
                jwtService.generateAccessToken(
                        user.getUsername(),
                        user.getRole()
                );

        // Generate JWT Refresh Token
        String refreshToken =
                jwtService.generateRefreshToken(
                        user.getUsername()
                );

        // Store tokens in session temporarily
        session.setAttribute("accessToken", accessToken);
        session.setAttribute("refreshToken", refreshToken);
        session.setAttribute("user", user);

        if (user.getRole().equals("STUDENT")) {
            return "redirect:/student";
        }

        if (user.getRole().equals("STAFF")) {
            return "redirect:/staff";
        }

        if (user.getRole().equals("ADMIN")) {
            return "redirect:/admin";
        }

        model.addAttribute("error", "Invalid role");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}