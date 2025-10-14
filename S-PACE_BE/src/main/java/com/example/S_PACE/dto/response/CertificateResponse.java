package com.example.S_PACE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private UUID certificatesId;
    private UUID eventId;
    private String eventName;
    private UUID userId;
    private String userName;
    private String certificateFilePath;
    private String certificateCode;
    private LocalDate issuedDate;
    private String issuedBy;
}
