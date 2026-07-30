package com.eventmanagement.api.service.impl;

import com.eventmanagement.api.dto.request.EventInquiryCreateRequest;
import com.eventmanagement.api.dto.request.EventInquiryUpdateRequest;
import com.eventmanagement.api.dto.request.StatusUpdateRequest;
import com.eventmanagement.api.dto.response.EventInquiryResponse;
import com.eventmanagement.api.dto.response.PagedResponse;
import com.eventmanagement.api.entity.EventInquiry;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import com.eventmanagement.api.entity.Role;
import com.eventmanagement.api.entity.User;
import com.eventmanagement.api.exception.ResourceNotFoundException;
import com.eventmanagement.api.exception.UnauthorizedAccessException;
import com.eventmanagement.api.repository.EventInquiryRepository;
import com.eventmanagement.api.repository.UserRepository;
import com.eventmanagement.api.service.EventInquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventInquiryServiceImpl implements EventInquiryService {

    private final EventInquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EventInquiryResponse createInquiry(EventInquiryCreateRequest request, String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);

        EventInquiry inquiry = EventInquiry.builder()
                .user(currentUser)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .location(request.getLocation())
                .estimatedBudget(request.getEstimatedBudget())
                .guestCount(request.getGuestCount())
                .status(InquiryStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .build();

        EventInquiry savedInquiry = inquiryRepository.save(inquiry);
        return mapToResponse(savedInquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public EventInquiryResponse getInquiryById(Long id, String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        EventInquiry inquiry = getInquiryEntityById(id);

        verifyOwnershipOrAdmin(inquiry, currentUser);

        return mapToResponse(inquiry);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EventInquiryResponse> getUserInquiries(String currentUserEmail, Pageable pageable) {
        User currentUser = getUserByEmail(currentUserEmail);
        Page<EventInquiry> inquiries = inquiryRepository.findByUserId(currentUser.getId(), pageable);
        return mapToPagedResponse(inquiries);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EventInquiryResponse> getAllInquiries(InquiryStatus status, EventType eventType, Pageable pageable) {
        Page<EventInquiry> inquiries;
        if (status != null && eventType != null) {
            inquiries = inquiryRepository.findByStatus(status, pageable);
        } else if (status != null) {
            inquiries = inquiryRepository.findByStatus(status, pageable);
        } else if (eventType != null) {
            inquiries = inquiryRepository.findByEventType(eventType, pageable);
        } else {
            inquiries = inquiryRepository.findAll(pageable);
        }
        return mapToPagedResponse(inquiries);
    }

    @Override
    @Transactional
    public EventInquiryResponse updateInquiry(Long id, EventInquiryUpdateRequest request, String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        EventInquiry inquiry = getInquiryEntityById(id);

        verifyOwnershipOrAdmin(inquiry, currentUser);

        inquiry.setCustomerName(request.getCustomerName());
        inquiry.setCustomerEmail(request.getCustomerEmail());
        inquiry.setCustomerPhone(request.getCustomerPhone());
        inquiry.setEventType(request.getEventType());
        inquiry.setEventDate(request.getEventDate());
        inquiry.setLocation(request.getLocation());
        inquiry.setEstimatedBudget(request.getEstimatedBudget());
        inquiry.setGuestCount(request.getGuestCount());
        inquiry.setSpecialRequests(request.getSpecialRequests());

        EventInquiry updatedInquiry = inquiryRepository.save(inquiry);
        return mapToResponse(updatedInquiry);
    }

    @Override
    @Transactional
    public EventInquiryResponse updateInquiryStatus(Long id, StatusUpdateRequest request) {
        EventInquiry inquiry = getInquiryEntityById(id);
        inquiry.setStatus(request.getStatus());
        EventInquiry updatedInquiry = inquiryRepository.save(inquiry);
        return mapToResponse(updatedInquiry);
    }

    @Override
    @Transactional
    public void deleteInquiry(Long id, String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        EventInquiry inquiry = getInquiryEntityById(id);

        verifyOwnershipOrAdmin(inquiry, currentUser);

        inquiryRepository.delete(inquiry);
    }

    /**
     * Enforce strict resource authorization (IDOR protection).
     * Non-admin users are restricted to accessing/modifying only their own inquiries.
     */
    private void verifyOwnershipOrAdmin(EventInquiry inquiry, User currentUser) {
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        boolean isOwner = Objects.equals(inquiry.getUser().getId(), currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedAccessException("Access denied: You do not have permission to access or modify this inquiry.");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private EventInquiry getInquiryEntityById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EventInquiry", "id", id));
    }

    private EventInquiryResponse mapToResponse(EventInquiry inquiry) {
        return EventInquiryResponse.builder()
                .id(inquiry.getId())
                .ownerUserId(inquiry.getUser().getId())
                .ownerUserEmail(inquiry.getUser().getEmail())
                .customerName(inquiry.getCustomerName())
                .customerEmail(inquiry.getCustomerEmail())
                .customerPhone(inquiry.getCustomerPhone())
                .eventType(inquiry.getEventType())
                .eventDate(inquiry.getEventDate())
                .location(inquiry.getLocation())
                .estimatedBudget(inquiry.getEstimatedBudget())
                .guestCount(inquiry.getGuestCount())
                .status(inquiry.getStatus())
                .specialRequests(inquiry.getSpecialRequests())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }

    private PagedResponse<EventInquiryResponse> mapToPagedResponse(Page<EventInquiry> inquiriesPage) {
        List<EventInquiryResponse> content = inquiriesPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<EventInquiryResponse>builder()
                .content(content)
                .pageNumber(inquiriesPage.getNumber())
                .pageSize(inquiriesPage.getSize())
                .totalElements(inquiriesPage.getTotalElements())
                .totalPages(inquiriesPage.getTotalPages())
                .last(inquiriesPage.isLast())
                .build();
    }
}
