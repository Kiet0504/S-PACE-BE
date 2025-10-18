package com.example.S_PACE.dto.request;

import com.example.S_PACE.enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventStatusRequest {
    
    @NotNull(message = "Event status is required")
    private EventStatus status;
}

