package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.response.UserResponse;

public interface UserService {
    UserResponse getCurrentUserProfile(String email);
}
