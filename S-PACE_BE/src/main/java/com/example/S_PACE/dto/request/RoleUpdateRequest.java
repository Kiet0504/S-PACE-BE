package com.example.S_PACE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequest {
    
    @NotBlank(message = "Role name is required")
    @Pattern(regexp = "^(EVENT_MANAGER|COLLABORATOR)$", 
             message = "Role must be EVENT_MANAGER or COLLABORATOR")
    private String roleName;
}
