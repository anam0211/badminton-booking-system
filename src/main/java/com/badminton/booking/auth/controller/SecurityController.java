package com.badminton.booking.auth.controller;

import com.badminton.booking.auth.dto.LoginRequest;
import com.badminton.booking.auth.dto.TokenResponse;
import com.badminton.booking.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class SecurityController {

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponse loginApi(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public Map<String, String> logoutApi() {
        authService.logout();
        return Map.of("message", "Logged out");
    }
}
