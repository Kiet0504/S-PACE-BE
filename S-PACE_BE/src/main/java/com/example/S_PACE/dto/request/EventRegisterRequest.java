package com.example.S_PACE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegisterRequest {

    @NotNull(message = "Event ID is required")
    UUID eventId;

    @NotBlank(message = "Full name is required")
    String fullName;

    @NotBlank(message = "Gender is required")
    String gender;

    @NotBlank(message = "Profession is required")
    String profession;

    @NotBlank(message = "Phone number is required")
    String phoneNumber;

    @NotBlank(message = "Address is required")
    String address;

    @NotBlank(message = "Reason for participation is required")
    String reasonForParticipation;

    String filePath; // Optional - for CV/Portfolio
} 