package com.example.S_PACE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionDTO {
    private UUID userId;
    private UUID planId;
    private String planName;
    private Integer maxRecruitmentLimit;
    private LocalDateTime purchasedAt;
}

