package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.request.EventInquiryCreateRequest;
import com.eventmanagement.api.dto.response.EventInquiryResponse;
import com.eventmanagement.api.entity.EventInquiry;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import com.eventmanagement.api.entity.Role;
import com.eventmanagement.api.entity.User;
import com.eventmanagement.api.exception.UnauthorizedAccessException;
import com.eventmanagement.api.repository.EventInquiryRepository;
import com.eventmanagement.api.repository.UserRepository;
import com.eventmanagement.api.service.impl.EventInquiryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EventInquiryServiceTest {

    @Mock
    private EventInquiryRepository inquiryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventInquiryServiceImpl inquiryService;

    private User ownerUser;
    private User otherUser;
    private EventInquiry sampleInquiry;

    @BeforeEach
    void setUp() {
        ownerUser = User.builder()
                .id(1L)
                .email("owner@example.com")
                .fullName("Owner User")
                .role(Role.ROLE_USER)
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .fullName("Other User")
                .role(Role.ROLE_USER)
                .build();

        sampleInquiry = EventInquiry.builder()
                .id(100L)
                .user(ownerUser)
                .customerName("Owner User")
                .customerEmail("owner@example.com")
                .customerPhone("+15551234567")
                .eventType(EventType.WEDDING)
                .eventDate(LocalDate.now().plusMonths(4))
                .location("Grand Ballroom")
                .estimatedBudget(new BigDecimal("20000.00"))
                .guestCount(120)
                .status(InquiryStatus.PENDING)
                .build();
    }

    @Test
    void createInquiry_ShouldSaveAndReturnResponse() {
        EventInquiryCreateRequest request = EventInquiryCreateRequest.builder()
                .customerName("Owner User")
                .customerEmail("owner@example.com")
                .customerPhone("+15551234567")
                .eventType(EventType.WEDDING)
                .eventDate(LocalDate.now().plusMonths(4))
                .location("Grand Ballroom")
                .estimatedBudget(new BigDecimal("20000.00"))
                .guestCount(120)
                .build();

        given(userRepository.findByEmail("owner@example.com")).willReturn(Optional.of(ownerUser));
        given(inquiryRepository.save(any(EventInquiry.class))).willReturn(sampleInquiry);

        EventInquiryResponse response = inquiryService.createInquiry(request, "owner@example.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(EventType.WEDDING, response.getEventType());
        assertEquals(InquiryStatus.PENDING, response.getStatus());
    }

    @Test
    void getInquiryById_ShouldThrowUnauthorizedAccessException_WhenUserIsNotOwnerAndNotAdmin() {
        given(userRepository.findByEmail("other@example.com")).willReturn(Optional.of(otherUser));
        given(inquiryRepository.findById(100L)).willReturn(Optional.of(sampleInquiry));

        assertThrows(UnauthorizedAccessException.class, () -> {
            inquiryService.getInquiryById(100L, "other@example.com");
        });
    }
}
