package com.example.S_PACE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {
    
    @NotNull(message = "Event ID is required")
    private UUID eventId;
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotBlank(message = "Certificate code is required")
    private String certificateCode;
    
    @NotNull(message = "Issued date is required")
    private LocalDate issuedDate;
    
    private String issuedBy;
}
