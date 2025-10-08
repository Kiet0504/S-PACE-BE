package com.example.S_PACE.dto.response;

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
public class CompanyResponse {
    private UUID companyId;
    private String companyName;
    private String description;
    private String address;
    private String contactInfo;
    private String website;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
