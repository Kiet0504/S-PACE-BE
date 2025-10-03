package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLogsResponse {
    private UUID attendanceLogsId;
    private UUID eventId;
    private String eventName;
    private UUID userId;
    private String userName;
    private String userEmail;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
}
