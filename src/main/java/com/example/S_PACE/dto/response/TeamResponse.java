package com.example.S_PACE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private UUID teamId;
    private UUID eventId;
    private String teamName;
    private int quantity;
    private LocalDateTime createdAt;
    private List<UserResponse> members;
}