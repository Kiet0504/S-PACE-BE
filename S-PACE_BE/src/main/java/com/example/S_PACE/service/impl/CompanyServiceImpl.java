package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.CompanyRequest;
import com.example.S_PACE.dto.response.CompanyResponse;
import com.example.S_PACE.enums.CompanyStatus;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.repository.CompanyRepository;
import com.example.S_PACE.service.CompanyService;
import com.example.S_PACE.service.CompanyAutoApprovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyAutoApprovalService autoApprovalService;

    @Override
    @Transactional
    public CompanyResponse createCompany(CompanyRequest companyRequest) {
        logger.info("Creating new company: {}", companyRequest.getCompanyName());
        
        // Check if company with same name already exists
        if (existsByName(companyRequest.getCompanyName())) {
            throw new IllegalArgumentException("Company with name '" + companyRequest.getCompanyName() + "' already exists");
        }
        
        // Create new company
        Company company = new Company();
        company.setCompanyName(companyRequest.getCompanyName());
        company.setAddress(companyRequest.getAddress());
        company.setStatus(companyRequest.getStatus() != null ? companyRequest.getStatus() : CompanyStatus.PENDING_APPROVAL);
        
        // Calculate validation score and check auto-approval
        Integer validationScore = autoApprovalService.calculateValidationScore(companyRequest);
        company.setValidationScore(validationScore);
        
        // Set validation details
        Map<String, Object> validationDetails = autoApprovalService.getValidationDetails(companyRequest);
        company.setValidationDetails(validationDetails);
        
        // Check if eligible for auto-approval
        if (autoApprovalService.isEligibleForAutoApproval(companyRequest)) {
            company.setStatus(CompanyStatus.ACTIVE);
            company.setIsAutoApproved(true);
            company.setAutoApprovalReason("Tự động duyệt dựa trên điểm validation: " + validationScore);
            logger.info("Company '{}' auto-approved with score: {}", companyRequest.getCompanyName(), validationScore);
        }
        
        // Save company
        Company savedCompany = companyRepository.save(company);
        logger.info("Company created successfully with ID: {}", savedCompany.getCompanyId());
        
        return mapToResponse(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID companyId) {
        logger.info("Fetching company by ID: {}", companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        
        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        logger.info("Fetching all companies");
        
        return companyRepository.findAll()
                .stream()
                .filter(company -> company.getStatus() != CompanyStatus.DELETED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(UUID companyId, CompanyRequest companyRequest) {
        logger.info("Updating company with ID: {}", companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        
        if (company.getStatus() == CompanyStatus.DELETED) {
            throw new IllegalArgumentException("Cannot update deleted company");
        }
        
        // Check if name is being changed and if it already exists
        if (!company.getCompanyName().equals(companyRequest.getCompanyName())) {
            if (existsByName(companyRequest.getCompanyName())) {
                throw new IllegalArgumentException("Company with name '" + companyRequest.getCompanyName() + "' already exists");
            }
        }
        
        // Update company fields
        company.setCompanyName(companyRequest.getCompanyName());
        company.setAddress(companyRequest.getAddress());
        if (companyRequest.getStatus() != null) {
            company.setStatus(companyRequest.getStatus());
        }
        
        Company updatedCompany = companyRepository.save(company);
        logger.info("Company updated successfully with ID: {}", updatedCompany.getCompanyId());
        
        return mapToResponse(updatedCompany);
    }

    @Override
    @Transactional
    public void deleteCompany(UUID companyId) {
        logger.info("Soft deleting company with ID: {}", companyId);
        
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        
        if (company.getStatus() == CompanyStatus.DELETED) {
            throw new IllegalArgumentException("Company is already deleted");
        }
        
        // Soft delete by changing status to DELETED
        company.setStatus(CompanyStatus.DELETED);
        companyRepository.save(company);
        
        logger.info("Company soft deleted successfully with ID: {}", companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getCompaniesByStatus(CompanyStatus status) {
        logger.info("Fetching companies by status: {}", status);
        
        return companyRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> getActiveCompanies() {
        logger.info("Fetching active companies");
        
        return companyRepository.findActiveCompanies()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyResponse> searchCompaniesByName(String name) {
        logger.info("Searching companies by name: {}", name);
        
        return companyRepository.findByCompanyNameContainingIgnoreCase(name)
                .stream()
                .filter(company -> company.getStatus() != CompanyStatus.DELETED)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyResponse getCompanyByName(String name) {
        logger.info("Fetching company by name: {}", name);
        
        Company company = companyRepository.findByCompanyNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with name: " + name));
        
        return mapToResponse(company);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID companyId) {
        return companyRepository.existsById(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String companyName) {
        return companyRepository.findByCompanyNameIgnoreCase(companyName).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCreateEvents(UUID companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        return company.isPresent() && company.get().getStatus().canCreateEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateValidationScore(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        return autoApprovalService.calculateValidationScore(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getValidationDetails(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        return autoApprovalService.getValidationDetails(company);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEligibleForAutoApproval(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        return autoApprovalService.isEligibleForAutoApproval(company);
    }

    @Override
    @Transactional
    public void updateValidationScore(UUID companyId) {
        autoApprovalService.updateValidationScore(companyId);
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .address(company.getAddress())
                .status(company.getStatus())
                .build();
    }
}

