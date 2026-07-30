package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.request.LoginRequest;
import com.eventmanagement.api.dto.request.RegisterRequest;
import com.eventmanagement.api.dto.response.AuthResponse;
import com.eventmanagement.api.dto.response.UserResponse;
import com.eventmanagement.api.service.AuthService;
import com.eventmanagement.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Endpoints for User Registration, Authentication, and Session Profile")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account", description = "Creates a new user account with default ROLE_USER or ROLE_ADMIN if admin key is supplied.")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT token", description = "Validates user credentials and returns a Bearer JWT token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Fetches the profile details of the currently authenticated user.")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
