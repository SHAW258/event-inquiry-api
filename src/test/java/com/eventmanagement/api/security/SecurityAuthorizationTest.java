package com.eventmanagement.api.security;

import com.eventmanagement.api.entity.EventInquiry;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import com.eventmanagement.api.entity.Role;
import com.eventmanagement.api.entity.User;
import com.eventmanagement.api.repository.EventInquiryRepository;
import com.eventmanagement.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventInquiryRepository inquiryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long user1InquiryId;

    @BeforeEach
    void setUp() {
        inquiryRepository.deleteAll();
        userRepository.deleteAll();

        User user1 = userRepository.save(User.builder()
                .fullName("User One")
                .email("user1@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .build());

        User user2 = userRepository.save(User.builder()
                .fullName("User Two")
                .email("user2@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ROLE_USER)
                .build());

        User admin = userRepository.save(User.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ROLE_ADMIN)
                .build());

        EventInquiry inquiry = inquiryRepository.save(EventInquiry.builder()
                .user(user1)
                .customerName("User One")
                .customerEmail("user1@example.com")
                .customerPhone("+15550001111")
                .eventType(EventType.WEDDING)
                .eventDate(LocalDate.now().plusMonths(3))
                .location("Downtown Plaza")
                .estimatedBudget(new BigDecimal("15000.00"))
                .guestCount(100)
                .status(InquiryStatus.PENDING)
                .build());

        user1InquiryId = inquiry.getId();
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"USER"})
    void getInquiryById_ShouldSucceed_WhenUserIsOwner() throws Exception {
        mockMvc.perform(get("/api/v1/inquiries/" + user1InquiryId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user2@example.com", roles = {"USER"})
    void getInquiryById_ShouldReturn403Forbidden_WhenUserIsNotOwner (IDOR Protection) () throws Exception {
        // IDOR attack simulation: user2 attempts to access user1's inquiry ID
        mockMvc.perform(get("/api/v1/inquiries/" + user1InquiryId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void getInquiryById_ShouldSucceed_WhenUserIsAdmin() throws Exception {
        // Admin can access any user's inquiry
        mockMvc.perform(get("/api/v1/inquiries/" + user1InquiryId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1@example.com", roles = {"USER"})
    void updateInquiryStatus_ShouldReturn403Forbidden_WhenNonAdminAttemptsStatusUpdate() throws Exception {
        // Only ADMIN is authorized to invoke PATCH /api/v1/inquiries/{id}/status
        mockMvc.perform(patch("/api/v1/inquiries/" + user1InquiryId + "/status")
                        .contentType("application/json")
                        .content("{\"status\": \"CONFIRMED\"}"))
                .andExpect(status().isForbidden());
    }
}
