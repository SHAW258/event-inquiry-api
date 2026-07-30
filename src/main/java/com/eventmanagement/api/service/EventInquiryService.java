package com.eventmanagement.api.service;

import com.eventmanagement.api.dto.request.EventInquiryCreateRequest;
import com.eventmanagement.api.dto.request.EventInquiryUpdateRequest;
import com.eventmanagement.api.dto.request.StatusUpdateRequest;
import com.eventmanagement.api.dto.response.EventInquiryResponse;
import com.eventmanagement.api.dto.response.PagedResponse;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import org.springframework.data.domain.Pageable;

public interface EventInquiryService {

    EventInquiryResponse createInquiry(EventInquiryCreateRequest request, String currentUserEmail);

    EventInquiryResponse getInquiryById(Long id, String currentUserEmail);

    PagedResponse<EventInquiryResponse> getUserInquiries(String currentUserEmail, Pageable pageable);

    PagedResponse<EventInquiryResponse> getAllInquiries(InquiryStatus status, EventType eventType, Pageable pageable);

    EventInquiryResponse updateInquiry(Long id, EventInquiryUpdateRequest request, String currentUserEmail);

    EventInquiryResponse updateInquiryStatus(Long id, StatusUpdateRequest request);

    void deleteInquiry(Long id, String currentUserEmail);
}
