package com.example.S_PACE.dto.request;

import com.example.S_PACE.validator.ValidCompanyAutoApprovalRule;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidCompanyAutoApprovalRule
public class CompanyAutoApprovalRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(max = 255, message = "Rule name must not exceed 255 characters")
    private String ruleName;

    @Size(max = 1000, message = "Rule description must not exceed 1000 characters")
    private String ruleDescription;

    @NotBlank(message = "Field name is required")
    @Size(max = 100, message = "Field name must not exceed 100 characters")
    private String fieldName;

    @NotBlank(message = "Field type is required")
    @Size(max = 50, message = "Field type must not exceed 50 characters")
    private String fieldType;

    @NotBlank(message = "Operator is required")
    @Size(max = 20, message = "Operator must not exceed 20 characters")
    private String operator;

    @Size(max = 1000, message = "Expected value must not exceed 1000 characters")
    private String expectedValue;

    @Builder.Default
    private Boolean isActive = true;

    @Min(value = 0, message = "Priority must be non-negative")
    @Max(value = 1000, message = "Priority must not exceed 1000")
    @Builder.Default
    private Integer priority = 0;
}
