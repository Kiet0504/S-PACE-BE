package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID eventId;
    private String eventName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Integer maxParticipants;
    private EventStatus status;
    private String requirements;
    private String contactInfo;
    private String picture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
