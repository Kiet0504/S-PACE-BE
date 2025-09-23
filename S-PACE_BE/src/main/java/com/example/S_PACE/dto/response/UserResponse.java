package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.UserStatus;
import com.example.S_PACE.pojo.Role;
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
public class UserResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createdAt;
    private Role role;
    
    // Exclude sensitive information like password
}
