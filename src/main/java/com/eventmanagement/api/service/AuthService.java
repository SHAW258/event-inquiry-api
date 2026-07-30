package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.request.LoginRequest;
import com.eventmanagement.api.dto.request.RegisterRequest;
import com.eventmanagement.api.dto.response.AuthResponse;
import com.eventmanagement.api.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
