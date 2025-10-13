package com.example.S_PACE.dto.request;

import com.example.S_PACE.enums.CompanyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String companyName;
    
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    
    private CompanyStatus status;
}
