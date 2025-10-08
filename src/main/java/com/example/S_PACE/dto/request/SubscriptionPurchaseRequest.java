package com.example.S_PACE.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPurchaseRequest {

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    private String returnUrl;

    private String cancelUrl;

    private String description;
}