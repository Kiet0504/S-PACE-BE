package com.example.S_PACE.dto.response;

import com.example.S_PACE.pojo.CompanyAutoApprovalRule;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAutoApprovalRuleResponse {

    private UUID ruleId;
    private String ruleName;
    private String ruleDescription;
    private String fieldName;
    private String fieldType;
    private String operator;
    private String expectedValue;
    private Boolean isActive;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyAutoApprovalRuleResponse fromEntity(CompanyAutoApprovalRule rule) {
        return CompanyAutoApprovalRuleResponse.builder()
                .ruleId(rule.getRuleId())
                .ruleName(rule.getRuleName())
                .ruleDescription(rule.getRuleDescription())
                .fieldName(rule.getFieldName())
                .fieldType(rule.getFieldType())
                .operator(rule.getOperator())
                .expectedValue(rule.getExpectedValue())
                .isActive(rule.getIsActive())
                .priority(rule.getPriority())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
