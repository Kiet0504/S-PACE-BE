package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.EventTasksStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTasksResponse {
    private UUID eventTasksId;
    private UUID teamId;
    private String teamName;
    private UUID assignedToUserId;
    private String assignedToName;
    private String assignedToEmail;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private EventTasksStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}