package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.request.LoginRequest;
import com.eventmanagement.api.dto.request.RegisterRequest;
import com.eventmanagement.api.dto.response.AuthResponse;
import com.eventmanagement.api.dto.response.UserResponse;
import com.eventmanagement.api.entity.Role;
import com.eventmanagement.api.service.AuthService;
import com.eventmanagement.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Test
    void register_ShouldReturn201_WhenRequestIsValid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Alice Wonderland")
                .email("alice@example.com")
                .password("password123")
                .build();

        UserResponse response = UserResponse.builder()
                .id(10L)
                .fullName("Alice Wonderland")
                .email("alice@example.com")
                .role(Role.ROLE_USER)
                .build();

        given(authService.register(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void login_ShouldReturn200AndToken_WhenCredentialsAreValid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("alice@example.com")
                .password("password123")
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(10L)
                .fullName("Alice Wonderland")
                .email("alice@example.com")
                .role(Role.ROLE_USER)
                .build();

        AuthResponse authResponse = AuthResponse.of("dummy-jwt-token", userResponse);

        given(authService.login(any(LoginRequest.class))).willReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("dummy-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}
