package com.example.S_PACE.dto.request;

import com.example.S_PACE.enums.EventStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EventRequest {
    private String eventName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Integer maxParticipants;
    private EventStatus status;
    private String requirements;
    private String contactInfo;
}
