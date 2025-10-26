package com.example.S_PACE.controller;

import com.example.S_PACE.dto.request.CompanyRequest;
import com.example.S_PACE.dto.response.CompanyResponse;
import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.enums.CompanyStatus;
import com.example.S_PACE.service.CompanyService;
import com.example.S_PACE.service.CompanyAutoApprovalService;
import com.example.S_PACE.pojo.CompanyAutoApprovalRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/companies")
@Tag(name = "Company Management", description = "Company management APIs")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyAutoApprovalService autoApprovalService;

    @PostMapping
    @Operation(summary = "Create new company", description = "Create a new company (Admin/Company Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Company created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or company already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyRequest companyRequest) {
        try {
            logger.info("Creating new company: {}", companyRequest.getCompanyName());
            CompanyResponse createdCompany = companyService.createCompany(companyRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Company created successfully", createdCompany));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating company: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create company", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get all companies", description = "Retrieve all companies (excludes deleted companies)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<CompanyResponse>>> getAllCompanies() {
        try {
            logger.info("Fetching all companies");
            List<CompanyResponse> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Companies retrieved successfully", companies));
        } catch (Exception ex) {
            logger.error("Error fetching all companies: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch companies", null));
        }
    }

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company by ID", description = "Retrieve a specific company by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<CompanyResponse>> getCompanyById(@PathVariable UUID companyId) {
        try {
            logger.info("Fetching company by ID: {}", companyId);
            CompanyResponse company = companyService.getCompanyById(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Company retrieved successfully", company));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch company", null));
        }
    }

    @PutMapping("/{companyId}")
    @Operation(summary = "Update company", description = "Update company information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<CompanyResponse>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyRequest companyRequest) {
        try {
            logger.info("Updating company with ID: {}", companyId);
            CompanyResponse updatedCompany = companyService.updateCompany(companyId, companyRequest);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Company updated successfully", updatedCompany));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company update validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update company", null));
        }
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Delete company", description = "Soft delete company by changing status to DELETED")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteCompany(@PathVariable UUID companyId) {
        try {
            logger.info("Deleting company with ID: {}", companyId);
            companyService.deleteCompany(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Company deleted successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company deletion validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error deleting company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to delete company", null));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get companies by status", description = "Retrieve companies with a specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<List<CompanyResponse>>> getCompaniesByStatus(@PathVariable CompanyStatus status) {
        try {
            logger.info("Fetching companies by status: {}", status);
            List<CompanyResponse> companies = companyService.getCompaniesByStatus(status);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Companies retrieved successfully", companies));
        } catch (Exception ex) {
            logger.error("Error fetching companies by status {}: {}", status, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch companies", null));
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Get active companies", description = "Retrieve all active companies that can create events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active companies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<CompanyResponse>>> getActiveCompanies() {
        try {
            logger.info("Fetching active companies");
            List<CompanyResponse> companies = companyService.getActiveCompanies();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Active companies retrieved successfully", companies));
        } catch (Exception ex) {
            logger.error("Error fetching active companies: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch active companies", null));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search companies by name", description = "Search companies by name (case-insensitive)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Companies retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<List<CompanyResponse>>> searchCompaniesByName(
            @RequestParam String name) {
        try {
            logger.info("Searching companies by name: {}", name);
            List<CompanyResponse> companies = companyService.searchCompaniesByName(name);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Companies retrieved successfully", companies));
        } catch (Exception ex) {
            logger.error("Error searching companies by name {}: {}", name, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to search companies", null));
        }
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get company by name", description = "Retrieve a specific company by its exact name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Company retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<CompanyResponse>> getCompanyByName(@PathVariable String name) {
        try {
            logger.info("Fetching company by name: {}", name);
            CompanyResponse company = companyService.getCompanyByName(name);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Company retrieved successfully", company));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error fetching company by name {}: {}", name, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to fetch company", null));
        }
    }

    @GetMapping("/{companyId}/can-create-events")
    @Operation(summary = "Check if company can create events", description = "Check if a company has permission to create events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission check completed"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN', 'EVENT_MANAGER')")
    public ResponseEntity<ResponseDTO<Boolean>> canCreateEvents(@PathVariable UUID companyId) {
        try {
            logger.info("Checking if company {} can create events", companyId);
            boolean canCreate = companyService.canCreateEvents(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Permission check completed", canCreate));
        } catch (Exception ex) {
            logger.error("Error checking company permissions {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to check permissions", null));
        }
    }

    @PostMapping("/user-create")
    @Operation(summary = "Create company by user", description = "Allow users to create their own company during registration (no authentication required)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Company created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or company already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ResponseDTO<CompanyResponse>> createCompanyByUser(
            @Valid @RequestBody CompanyRequest companyRequest) {
        try {
            logger.info("User creating new company: {}", companyRequest.getCompanyName());
            
            // Set default status to ACTIVE for user-created companies
            if (companyRequest.getStatus() == null) {
                companyRequest.setStatus(com.example.S_PACE.enums.CompanyStatus.ACTIVE);
            }
            
            CompanyResponse createdCompany = companyService.createCompany(companyRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Company created successfully", createdCompany));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company creation validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error creating company: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create company", null));
        }
    }

    // Auto-approval validation endpoints
    @GetMapping("/{companyId}/validation-score")
    @Operation(summary = "Get company validation score", description = "Get the validation score for a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation score retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<Integer>> getValidationScore(@PathVariable UUID companyId) {
        try {
            logger.info("Getting validation score for company: {}", companyId);
            Integer score = companyService.calculateValidationScore(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Validation score retrieved successfully", score));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error getting validation score for company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get validation score", null));
        }
    }

    @GetMapping("/{companyId}/validation-details")
    @Operation(summary = "Get company validation details", description = "Get detailed validation information for a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<Map<String, Object>>> getValidationDetails(@PathVariable UUID companyId) {
        try {
            logger.info("Getting validation details for company: {}", companyId);
            Map<String, Object> details = companyService.getValidationDetails(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Validation details retrieved successfully", details));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error getting validation details for company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get validation details", null));
        }
    }

    @GetMapping("/{companyId}/auto-approval-eligible")
    @Operation(summary = "Check auto-approval eligibility", description = "Check if a company is eligible for auto-approval")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auto-approval eligibility checked successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<ResponseDTO<Boolean>> checkAutoApprovalEligibility(@PathVariable UUID companyId) {
        try {
            logger.info("Checking auto-approval eligibility for company: {}", companyId);
            boolean eligible = companyService.isEligibleForAutoApproval(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Auto-approval eligibility checked successfully", eligible));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error checking auto-approval eligibility for company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to check auto-approval eligibility", null));
        }
    }

    @PostMapping("/{companyId}/update-validation")
    @Operation(summary = "Update company validation score", description = "Manually update the validation score for a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation score updated successfully"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> updateValidationScore(@PathVariable UUID companyId) {
        try {
            logger.info("Updating validation score for company: {}", companyId);
            companyService.updateValidationScore(companyId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Validation score updated successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Company not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating validation score for company {}: {}", companyId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update validation score", null));
        }
    }

    // Auto-approval rules management endpoints
    @GetMapping("/auto-approval-rules")
    @Operation(summary = "Get all auto-approval rules", description = "Get all auto-approval rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auto-approval rules retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<CompanyAutoApprovalRule>>> getAllAutoApprovalRules() {
        try {
            logger.info("Getting all auto-approval rules");
            List<CompanyAutoApprovalRule> rules = autoApprovalService.getAllRules();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Auto-approval rules retrieved successfully", rules));
        } catch (Exception ex) {
            logger.error("Error getting auto-approval rules: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get auto-approval rules", null));
        }
    }

    @GetMapping("/auto-approval-rules/{ruleId}")
    @Operation(summary = "Get auto-approval rule by ID", description = "Get a specific auto-approval rule by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auto-approval rule retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Rule not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> getAutoApprovalRuleById(@PathVariable UUID ruleId) {
        try {
            logger.info("Getting auto-approval rule by ID: {}", ruleId);
            CompanyAutoApprovalRule rule = autoApprovalService.getRuleById(ruleId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Auto-approval rule retrieved successfully", rule));
        } catch (IllegalArgumentException ex) {
            logger.warn("Rule not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error getting auto-approval rule {}: {}", ruleId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to get auto-approval rule", null));
        }
    }

    @PostMapping("/auto-approval-rules")
    @Operation(summary = "Create auto-approval rule", description = "Create a new auto-approval rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Auto-approval rule created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> createAutoApprovalRule(
            @Valid @RequestBody CompanyAutoApprovalRule rule) {
        try {
            logger.info("Creating new auto-approval rule: {}", rule.getRuleName());
            CompanyAutoApprovalRule createdRule = autoApprovalService.createRule(rule);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(true, "Auto-approval rule created successfully", createdRule));
        } catch (Exception ex) {
            logger.error("Error creating auto-approval rule: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to create auto-approval rule", null));
        }
    }

    @PutMapping("/auto-approval-rules/{ruleId}")
    @Operation(summary = "Update auto-approval rule", description = "Update an existing auto-approval rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auto-approval rule updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Rule not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<CompanyAutoApprovalRule>> updateAutoApprovalRule(
            @PathVariable UUID ruleId,
            @Valid @RequestBody CompanyAutoApprovalRule rule) {
        try {
            logger.info("Updating auto-approval rule: {}", ruleId);
            CompanyAutoApprovalRule updatedRule = autoApprovalService.updateRule(ruleId, rule);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Auto-approval rule updated successfully", updatedRule));
        } catch (IllegalArgumentException ex) {
            logger.warn("Rule not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error updating auto-approval rule {}: {}", ruleId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to update auto-approval rule", null));
        }
    }

    @DeleteMapping("/auto-approval-rules/{ruleId}")
    @Operation(summary = "Delete auto-approval rule", description = "Delete an auto-approval rule (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auto-approval rule deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Rule not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<Void>> deleteAutoApprovalRule(@PathVariable UUID ruleId) {
        try {
            logger.info("Deleting auto-approval rule: {}", ruleId);
            autoApprovalService.deleteRule(ruleId);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Auto-approval rule deleted successfully", null));
        } catch (IllegalArgumentException ex) {
            logger.warn("Rule not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            logger.error("Error deleting auto-approval rule {}: {}", ruleId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to delete auto-approval rule", null));
        }
    }

    @GetMapping("/auto-approval-rules/search")
    @Operation(summary = "Search auto-approval rules", description = "Search auto-approval rules by name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Auto-approval rules retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<CompanyAutoApprovalRule>>> searchAutoApprovalRules(
            @RequestParam String name) {
        try {
            logger.info("Searching auto-approval rules by name: {}", name);
            List<CompanyAutoApprovalRule> rules = autoApprovalService.searchRulesByName(name);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Auto-approval rules retrieved successfully", rules));
        } catch (Exception ex) {
            logger.error("Error searching auto-approval rules by name {}: {}", name, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseDTO<>(false, "Failed to search auto-approval rules", null));
        }
    }
}
