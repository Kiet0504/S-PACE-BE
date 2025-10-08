package com.example.S_PACE.dto.request;

import com.example.S_PACE.enums.EventTasksStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTasksRequest {
    private UUID teamId;
    private UUID assignedToUserId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private EventTasksStatus status;
}