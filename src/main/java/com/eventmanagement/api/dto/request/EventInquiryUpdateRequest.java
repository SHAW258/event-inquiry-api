package com.eventmanagement.api.dto.request;

import com.eventmanagement.api.entity.EventType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInquiryUpdateRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate eventDate;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Estimated budget is required")
    @DecimalMin(value = "1.00", message = "Estimated budget must be at least 1.00")
    private BigDecimal estimatedBudget;

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @Size(max = 1000, message = "Special requests cannot exceed 1000 characters")
    private String specialRequests;
}
