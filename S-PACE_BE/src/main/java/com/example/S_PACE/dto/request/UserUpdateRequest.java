package com.example.S_PACE.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    String email;

    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address;

    String avatar;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    String gender;

    java.util.UUID companyId;
} 