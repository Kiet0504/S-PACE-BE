package com.example.S_PACE.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PurchasePlanRequest {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID planId;
}
