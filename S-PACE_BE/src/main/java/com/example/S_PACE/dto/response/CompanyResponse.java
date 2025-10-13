package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.CompanyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private UUID companyId;
    private String companyName;
    private String address;
    private CompanyStatus status;
}
