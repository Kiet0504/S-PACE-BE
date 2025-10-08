package com.example.S_PACE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {
    private UUID eventId;
    private String teamName;
    private int quantity;
}