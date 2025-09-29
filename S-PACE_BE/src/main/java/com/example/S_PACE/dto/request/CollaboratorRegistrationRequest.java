package com.example.S_PACE.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.*;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaboratorRegistrationRequest {

    @NotNull(message = "Event ID is required")
    UUID eventId;

    @NotNull(message = "User ID is required")
    UUID userId;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    String fullName;

    @NotBlank(message = "Gender is required")
    String gender;

    @NotBlank(message = "Profession is required")
    @Size(max = 100, message = "Profession must not exceed 100 characters")
    String profession;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10-15 digits")
    String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;

    @NotBlank(message = "Reason for participation is required")
    @Size(max = 1000, message = "Reason for participation must not exceed 1000 characters")
    String reasonForParticipation;

    String filePath; // CV and Portfolio file path
} 
