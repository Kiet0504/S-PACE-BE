package com.example.S_PACE.dto.request;

import lombok.Data;

@Data
public class CompanyRequest {
    private String companyName;
    private String description;
    private String address;
    private String contactInfo;
    private String website;
}
