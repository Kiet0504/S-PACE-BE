package com.example.S_PACE.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Entity
@Table(name = "company_auto_approval_rules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CompanyAutoApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rule_id")
    UUID ruleId;

    @Column(name = "rule_name", nullable = false, length = 255)
    String ruleName;

    @Column(name = "rule_description", columnDefinition = "TEXT")
    String ruleDescription;

    @Column(name = "field_name", nullable = false, length = 100)
    String fieldName;

    @Column(name = "field_type", nullable = false, length = 50)
    String fieldType;

    @Column(name = "operator", nullable = false, length = 20)
    String operator;

    @Column(name = "expected_value", columnDefinition = "TEXT")
    String expectedValue;

    @Column(name = "is_active")
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "priority")
    @Builder.Default
    Integer priority = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
