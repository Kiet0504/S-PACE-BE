package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.CompanyRequest;
import com.example.S_PACE.dto.response.CompanyResponse;
import com.example.S_PACE.enums.CompanyStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CompanyService {
    
    // CRUD operations
    CompanyResponse createCompany(CompanyRequest companyRequest);
    CompanyResponse getCompanyById(UUID companyId);
    List<CompanyResponse> getAllCompanies();
    CompanyResponse updateCompany(UUID companyId, CompanyRequest companyRequest);
    void deleteCompany(UUID companyId);
    
    // Business operations
    List<CompanyResponse> getCompaniesByStatus(CompanyStatus status);
    List<CompanyResponse> getActiveCompanies();
    List<CompanyResponse> searchCompaniesByName(String name);
    CompanyResponse getCompanyByName(String name);
    
    // Utility methods
    boolean existsById(UUID companyId);
    boolean existsByName(String companyName);
    boolean canCreateEvents(UUID companyId);
    
    // Auto-approval methods
    Integer calculateValidationScore(UUID companyId);
    Map<String, Object> getValidationDetails(UUID companyId);
    boolean isEligibleForAutoApproval(UUID companyId);
    void updateValidationScore(UUID companyId);
}



