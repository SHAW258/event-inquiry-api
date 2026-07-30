package com.eventmanagement.api.dto.request;

import com.eventmanagement.api.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "Inquiry status is required")
    private InquiryStatus status;
}
