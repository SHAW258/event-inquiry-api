package com.eventmanagement.api.controller;

import com.eventmanagement.api.dto.request.EventInquiryCreateRequest;
import com.eventmanagement.api.dto.request.EventInquiryUpdateRequest;
import com.eventmanagement.api.dto.request.StatusUpdateRequest;
import com.eventmanagement.api.dto.response.EventInquiryResponse;
import com.eventmanagement.api.dto.response.PagedResponse;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import com.eventmanagement.api.service.EventInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Event Inquiry Operations", description = "REST APIs for Event Inquiry creation, retrieval, status updates, and management")
public class EventInquiryController {

    private final EventInquiryService inquiryService;

    @PostMapping
    @Operation(summary = "Create a new event inquiry", description = "Allows authenticated users to submit a new event inquiry.")
    public ResponseEntity<EventInquiryResponse> createInquiry(
            @Valid @RequestBody EventInquiryCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventInquiryResponse response = inquiryService.createInquiry(request, userDetails.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @Operation(summary = "Get inquiries for the authenticated user", description = "Retrieves a paginated list of inquiries submitted by the current user.")
    public ResponseEntity<PagedResponse<EventInquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<EventInquiryResponse> response = inquiryService.getUserInquiries(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all inquiries (ADMIN ONLY)", description = "Allows ADMIN users to list and filter all event inquiries across the system.")
    public ResponseEntity<PagedResponse<EventInquiryResponse>> getAllInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) EventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<EventInquiryResponse> response = inquiryService.getAllInquiries(status, eventType, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inquiry by ID", description = "Fetches a specific inquiry by ID. Ownership or ADMIN role is strictly enforced.")
    public ResponseEntity<EventInquiryResponse> getInquiryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventInquiryResponse response = inquiryService.getInquiryById(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an inquiry", description = "Updates an existing inquiry. Only the owner or an ADMIN can update.")
    public ResponseEntity<EventInquiryResponse> updateInquiry(
            @PathVariable Long id,
            @Valid @RequestBody EventInquiryUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        EventInquiryResponse response = inquiryService.updateInquiry(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update inquiry status (ADMIN ONLY)", description = "Allows ADMIN users to change the status of an event inquiry (PENDING, CONFIRMED, etc.).")
    public ResponseEntity<EventInquiryResponse> updateInquiryStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        EventInquiryResponse response = inquiryService.updateInquiryStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an inquiry", description = "Deletes an inquiry by ID. Only the owner or an ADMIN can delete.")
    public ResponseEntity<Void> deleteInquiry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        inquiryService.deleteInquiry(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
