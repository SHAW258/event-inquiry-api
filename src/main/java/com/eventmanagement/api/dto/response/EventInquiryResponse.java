package com.eventmanagement.api.dto.response;

import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInquiryResponse {

    private Long id;
    private Long ownerUserId;
    private String ownerUserEmail;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private EventType eventType;
    private LocalDate eventDate;
    private String location;
    private BigDecimal estimatedBudget;
    private Integer guestCount;
    private InquiryStatus status;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
