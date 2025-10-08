package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.EventRegistrationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationResponse {

    UUID eventRegistrationId;
    UUID eventId;
    String eventTitle;
    UUID userId;
    String userEmail;
    LocalDateTime registrationDate;
    UUID reviewedBy;
    String reviewedByName;
    LocalDateTime reviewedAt;
    String reviewNotes;
    EventRegistrationStatus status;
    LocalDateTime updatedAt;
    
    // Collaborator information
    String fullName;
    String gender;
    String profession;
    String phoneNumber;
    String address;
    String reasonForParticipation;
    String filePath;
    Integer birthYear;
} 