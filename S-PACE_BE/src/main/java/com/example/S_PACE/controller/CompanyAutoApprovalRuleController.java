package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.CompanyAutoApprovalRuleRequest;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.pojo.CompanyAutoApprovalRule;
import com.example.S_PACE.service.CompanyAutoApprovalService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/company-auto-approval-rules")
public class CompanyAutoApprovalRuleController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyAutoApprovalRuleController.class);

    @Autowired
    private CompanyAutoApprovalService companyAutoApprovalService;

    /**
     * Tạo rule mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> createRule(
            @Valid @RequestBody CompanyAutoApprovalRuleRequest request) {
        try {
            logger.info("Creating new auto-approval rule: {}", request.getRuleName());
            
            CompanyAutoApprovalRule rule = CompanyAutoApprovalRule.builder()
                    .ruleName(request.getRuleName())
                    .ruleDescription(request.getRuleDescription())
                    .fieldName(request.getFieldName())
                    .fieldType(request.getFieldType())
                    .operator(request.getOperator())
                    .expectedValue(request.getExpectedValue())
                    .isActive(request.getIsActive())
                    .priority(request.getPriority())
                    .build();

            CompanyAutoApprovalRule createdRule = companyAutoApprovalService.createRule(rule);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rule created successfully", createdRule));
        } catch (Exception e) {
            logger.error("Error creating rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error creating rule: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy tất cả rules với phân trang
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<CompanyAutoApprovalRule>>> getAllRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "priority") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Boolean isActive) {
        try {
            logger.info("Getting all auto-approval rules - page: {}, size: {}", page, size);
            
            List<CompanyAutoApprovalRule> rules;
            if (isActive != null) {
                rules = isActive ? 
                    companyAutoApprovalService.getActiveRules() : 
                    companyAutoApprovalService.getAllRules().stream()
                        .filter(rule -> !rule.getIsActive())
                        .toList();
            } else {
                rules = companyAutoApprovalService.getAllRules();
            }
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rules retrieved successfully", rules));
        } catch (Exception e) {
            logger.error("Error getting rules: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error getting rules: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy rule theo ID
     */
    @GetMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> getRuleById(@PathVariable UUID ruleId) {
        try {
            logger.info("Getting rule by ID: {}", ruleId);
            
            CompanyAutoApprovalRule rule = companyAutoApprovalService.getRuleById(ruleId);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rule retrieved successfully", rule));
        } catch (IllegalArgumentException e) {
            logger.warn("Rule not found: {}", ruleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(false, "Rule not found", null));
        } catch (Exception e) {
            logger.error("Error getting rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error getting rule: " + e.getMessage(), null));
        }
    }

    /**
     * Cập nhật rule
     */
    @PutMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> updateRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody CompanyAutoApprovalRuleRequest request) {
        try {
            logger.info("Updating rule: {}", ruleId);
            
            CompanyAutoApprovalRule rule = CompanyAutoApprovalRule.builder()
                    .ruleName(request.getRuleName())
                    .ruleDescription(request.getRuleDescription())
                    .fieldName(request.getFieldName())
                    .fieldType(request.getFieldType())
                    .operator(request.getOperator())
                    .expectedValue(request.getExpectedValue())
                    .isActive(request.getIsActive())
                    .priority(request.getPriority())
                    .build();

            CompanyAutoApprovalRule updatedRule = companyAutoApprovalService.updateRule(ruleId, rule);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rule updated successfully", updatedRule));
        } catch (IllegalArgumentException e) {
            logger.warn("Rule not found for update: {}", ruleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(false, "Rule not found", null));
        } catch (Exception e) {
            logger.error("Error updating rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error updating rule: " + e.getMessage(), null));
        }
    }

    /**
     * Xóa rule (soft delete)
     */
    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteRule(@PathVariable UUID ruleId) {
        try {
            logger.info("Deleting rule: {}", ruleId);
            
            companyAutoApprovalService.deleteRule(ruleId);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rule deleted successfully", null));
        } catch (IllegalArgumentException e) {
            logger.warn("Rule not found for deletion: {}", ruleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseDTO.<Void>builder()
                            .success(false)
                            .message("Rule not found")
                            .build());
        } catch (Exception e) {
            logger.error("Error deleting rule: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error deleting rule: " + e.getMessage(), null));
        }
    }

    /**
     * Tìm kiếm rules theo tên
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<CompanyAutoApprovalRule>>> searchRules(
            @RequestParam String name) {
        try {
            logger.info("Searching rules by name: {}", name);
            
            List<CompanyAutoApprovalRule> rules = companyAutoApprovalService.searchRulesByName(name);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rules found successfully", rules));
        } catch (Exception e) {
            logger.error("Error searching rules: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error searching rules: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách rules đang active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<CompanyAutoApprovalRule>>> getActiveRules() {
        try {
            logger.info("Getting active rules");
            
            List<CompanyAutoApprovalRule> rules = companyAutoApprovalService.getActiveRules();
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Active rules retrieved successfully", rules));
        } catch (Exception e) {
            logger.error("Error getting active rules: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error getting active rules: " + e.getMessage(), null));
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa rule
     */
    @PatchMapping("/{ruleId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> toggleRuleStatus(@PathVariable UUID ruleId) {
        try {
            logger.info("Toggling rule status: {}", ruleId);
            
            CompanyAutoApprovalRule rule = companyAutoApprovalService.getRuleById(ruleId);
            rule.setIsActive(!rule.getIsActive());
            
            CompanyAutoApprovalRule updatedRule = companyAutoApprovalService.updateRule(ruleId, rule);
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Rule status updated successfully", updatedRule));
        } catch (IllegalArgumentException e) {
            logger.warn("Rule not found for status toggle: {}", ruleId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO<>(false, "Rule not found", null));
        } catch (Exception e) {
            logger.error("Error toggling rule status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error toggling rule status: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy thông tin về các field có thể sử dụng trong rules
     */
    @GetMapping("/available-fields")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<Map<String, Object>>>> getAvailableFields() {
        try {
            logger.info("Getting available fields for rules");
            
            List<Map<String, Object>> fields = List.of(
                Map.of(
                    "fieldName", "company_name",
                    "fieldType", "STRING",
                    "description", "Tên công ty",
                    "operators", List.of("NOT_EMPTY", "LENGTH_GREATER_THAN", "REGEX_MATCH", "EQUALS", "CONTAINS")
                ),
                Map.of(
                    "fieldName", "address",
                    "fieldType", "STRING", 
                    "description", "Địa chỉ công ty",
                    "operators", List.of("NOT_EMPTY", "LENGTH_GREATER_THAN", "REGEX_MATCH", "EQUALS", "CONTAINS")
                )
            );
            
            return ResponseEntity.ok(new ResponseDTO<>(true, "Available fields retrieved successfully", fields));
        } catch (Exception e) {
            logger.error("Error getting available fields: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(false, "Error getting available fields: " + e.getMessage(), null));
        }
    }
}
