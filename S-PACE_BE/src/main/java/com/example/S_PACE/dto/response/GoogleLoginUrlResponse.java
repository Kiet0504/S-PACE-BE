package com.example.S_PACE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginUrlResponse {
    private String loginUrl;
    private String redirectUri;
    private String message;
    private String clientId;
} 