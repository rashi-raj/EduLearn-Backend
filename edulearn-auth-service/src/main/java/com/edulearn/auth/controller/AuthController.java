package com.edulearn.auth.controller;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.dto.ForgotPasswordRequest;
import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
import com.edulearn.auth.dto.ResetPasswordRequest;
import com.edulearn.auth.dto.UserResponse;
import com.edulearn.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth Controller", description = "Authentication APIs for EduLearn users")
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get current logged-in user")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    @Operation(summary = "Health check endpoint")
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Auth service is working");
    }

    @Operation(summary = "Send forgot password email")
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @Operation(summary = "Reset password using token")
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}