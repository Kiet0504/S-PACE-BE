package com.example.S_PACE.dto.response;

import com.example.S_PACE.enums.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private boolean success;
    private int code;
    private String message;
    private String description;
    private String timestamp;
    
    public static ErrorResponse fromErrorStatus(ErrorStatus errorStatus) {
        return ErrorResponse.builder()
                .success(false)
                .code(errorStatus.getCode())
                .message(errorStatus.getMessage())
                .description(errorStatus.getDescription())
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
