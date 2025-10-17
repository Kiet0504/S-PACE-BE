package com.example.S_PACE.dto.request;

import com.example.S_PACE.enums.AttendanceStatus;
import com.example.S_PACE.enums.ParticipationStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttendanceLogsRequest {
    private UUID eventId;
    private UUID userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private ParticipationStatus participationStatus;
}
