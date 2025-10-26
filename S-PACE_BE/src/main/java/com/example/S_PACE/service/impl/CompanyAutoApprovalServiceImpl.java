package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.request.CompanyRequest;
import com.example.S_PACE.enums.CompanyStatus;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.pojo.CompanyAutoApprovalRule;
import com.example.S_PACE.repository.CompanyAutoApprovalRuleRepository;
import com.example.S_PACE.repository.CompanyRepository;
import com.example.S_PACE.service.CompanyAutoApprovalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
@Transactional
public class CompanyAutoApprovalServiceImpl implements CompanyAutoApprovalService {

    private static final Logger logger = LoggerFactory.getLogger(CompanyAutoApprovalServiceImpl.class);
    private static final Integer AUTO_APPROVAL_THRESHOLD = 30; // Ngưỡng điểm để tự động duyệt

    @Autowired
    private CompanyAutoApprovalRuleRepository ruleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Integer calculateValidationScore(Company company) {
        logger.info("Calculating validation score for company: {}", company.getCompanyId());
        
        List<CompanyAutoApprovalRule> activeRules = ruleRepository.findActiveRulesOrderByPriority();
        int totalScore = 0;

        for (CompanyAutoApprovalRule rule : activeRules) {
            if (evaluateRule(rule, company)) {
                totalScore += rule.getPriority();
                logger.debug("Rule '{}' passed, added {} points. Total: {}", 
                    rule.getRuleName(), rule.getPriority(), totalScore);
            }
        }

        logger.info("Validation score calculated: {} for company: {}", totalScore, company.getCompanyId());
        return totalScore;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateValidationScore(CompanyRequest companyRequest) {
        logger.info("Calculating validation score for company request: {}", companyRequest.getCompanyName());
        
        List<CompanyAutoApprovalRule> activeRules = ruleRepository.findActiveRulesOrderByPriority();
        int totalScore = 0;

        for (CompanyAutoApprovalRule rule : activeRules) {
            if (evaluateRuleForRequest(rule, companyRequest)) {
                totalScore += rule.getPriority();
                logger.debug("Rule '{}' passed, added {} points. Total: {}", 
                    rule.getRuleName(), rule.getPriority(), totalScore);
            }
        }

        logger.info("Validation score calculated: {} for company request: {}", totalScore, companyRequest.getCompanyName());
        return totalScore;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEligibleForAutoApproval(Company company) {
        Integer score = calculateValidationScore(company);
        boolean eligible = score >= AUTO_APPROVAL_THRESHOLD;
        
        logger.info("Company {} auto-approval eligibility: {} (score: {}/{})", 
            company.getCompanyId(), eligible, score, AUTO_APPROVAL_THRESHOLD);
        
        return eligible;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEligibleForAutoApproval(CompanyRequest companyRequest) {
        Integer score = calculateValidationScore(companyRequest);
        boolean eligible = score >= AUTO_APPROVAL_THRESHOLD;
        
        logger.info("Company request '{}' auto-approval eligibility: {} (score: {}/{})", 
            companyRequest.getCompanyName(), eligible, score, AUTO_APPROVAL_THRESHOLD);
        
        return eligible;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyAutoApprovalRule> getActiveRules() {
        return ruleRepository.findActiveRulesOrderByPriority();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getValidationDetails(Company company) {
        Map<String, Object> details = new HashMap<>();
        List<CompanyAutoApprovalRule> activeRules = ruleRepository.findActiveRulesOrderByPriority();
        List<Map<String, Object>> ruleResults = new ArrayList<>();
        
        int totalScore = 0;
        
        for (CompanyAutoApprovalRule rule : activeRules) {
            Map<String, Object> ruleResult = new HashMap<>();
            ruleResult.put("ruleName", rule.getRuleName());
            ruleResult.put("ruleDescription", rule.getRuleDescription());
            ruleResult.put("priority", rule.getPriority());
            ruleResult.put("operator", rule.getOperator());
            ruleResult.put("expectedValue", rule.getExpectedValue());
            
            boolean passed = evaluateRule(rule, company);
            ruleResult.put("passed", passed);
            
            if (passed) {
                totalScore += rule.getPriority();
            }
            
            ruleResults.add(ruleResult);
        }
        
        details.put("totalScore", totalScore);
        details.put("threshold", AUTO_APPROVAL_THRESHOLD);
        details.put("eligible", totalScore >= AUTO_APPROVAL_THRESHOLD);
        details.put("ruleResults", ruleResults);
        
        return details;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getValidationDetails(CompanyRequest companyRequest) {
        Map<String, Object> details = new HashMap<>();
        List<CompanyAutoApprovalRule> activeRules = ruleRepository.findActiveRulesOrderByPriority();
        List<Map<String, Object>> ruleResults = new ArrayList<>();
        
        int totalScore = 0;
        
        for (CompanyAutoApprovalRule rule : activeRules) {
            Map<String, Object> ruleResult = new HashMap<>();
            ruleResult.put("ruleName", rule.getRuleName());
            ruleResult.put("ruleDescription", rule.getRuleDescription());
            ruleResult.put("priority", rule.getPriority());
            ruleResult.put("operator", rule.getOperator());
            ruleResult.put("expectedValue", rule.getExpectedValue());
            
            boolean passed = evaluateRuleForRequest(rule, companyRequest);
            ruleResult.put("passed", passed);
            
            if (passed) {
                totalScore += rule.getPriority();
            }
            
            ruleResults.add(ruleResult);
        }
        
        details.put("totalScore", totalScore);
        details.put("threshold", AUTO_APPROVAL_THRESHOLD);
        details.put("eligible", totalScore >= AUTO_APPROVAL_THRESHOLD);
        details.put("ruleResults", ruleResults);
        
        return details;
    }

    @Override
    @Transactional
    public void updateValidationScore(UUID companyId) {
        logger.info("Updating validation score for company: {}", companyId);
        
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
        
        Integer newScore = calculateValidationScore(company);
        Map<String, Object> validationDetails = getValidationDetails(company);
        
        company.setValidationScore(newScore);
        
        try {
            company.setValidationDetails(objectMapper.writeValueAsString(validationDetails));
        } catch (JsonProcessingException e) {
            logger.error("Error serializing validation details: {}", e.getMessage());
        }
        
        // Kiểm tra điều kiện tự động duyệt
        if (newScore >= AUTO_APPROVAL_THRESHOLD && company.getStatus() == CompanyStatus.PENDING_APPROVAL) {
            company.setStatus(CompanyStatus.ACTIVE);
            company.setIsAutoApproved(true);
            company.setAutoApprovalReason("Tự động duyệt dựa trên điểm validation: " + newScore);
            logger.info("Company {} auto-approved with score: {}", companyId, newScore);
        }
        
        companyRepository.save(company);
    }

    @Override
    @Transactional
    public CompanyAutoApprovalRule createRule(CompanyAutoApprovalRule rule) {
        logger.info("Creating new auto-approval rule: {}", rule.getRuleName());
        return ruleRepository.save(rule);
    }

    @Override
    @Transactional
    public CompanyAutoApprovalRule updateRule(UUID ruleId, CompanyAutoApprovalRule rule) {
        logger.info("Updating auto-approval rule: {}", ruleId);
        
        CompanyAutoApprovalRule existingRule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + ruleId));
        
        existingRule.setRuleName(rule.getRuleName());
        existingRule.setRuleDescription(rule.getRuleDescription());
        existingRule.setFieldName(rule.getFieldName());
        existingRule.setFieldType(rule.getFieldType());
        existingRule.setOperator(rule.getOperator());
        existingRule.setExpectedValue(rule.getExpectedValue());
        existingRule.setIsActive(rule.getIsActive());
        existingRule.setPriority(rule.getPriority());
        
        return ruleRepository.save(existingRule);
    }

    @Override
    @Transactional
    public void deleteRule(UUID ruleId) {
        logger.info("Deleting auto-approval rule: {}", ruleId);
        
        CompanyAutoApprovalRule rule = ruleRepository.findById(ruleId)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + ruleId));
        
        rule.setIsActive(false);
        ruleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyAutoApprovalRule getRuleById(UUID ruleId) {
        return ruleRepository.findById(ruleId)
            .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + ruleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyAutoApprovalRule> getAllRules() {
        return ruleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyAutoApprovalRule> searchRulesByName(String name) {
        return ruleRepository.findByRuleNameContainingIgnoreCase(name);
    }

    private boolean evaluateRule(CompanyAutoApprovalRule rule, Company company) {
        String fieldValue = getFieldValue(rule.getFieldName(), company);
        return evaluateCondition(rule, fieldValue);
    }

    private boolean evaluateRuleForRequest(CompanyAutoApprovalRule rule, CompanyRequest companyRequest) {
        String fieldValue = getFieldValueFromRequest(rule.getFieldName(), companyRequest);
        return evaluateCondition(rule, fieldValue);
    }

    private String getFieldValue(String fieldName, Company company) {
        switch (fieldName) {
            case "company_name":
                return company.getCompanyName();
            case "address":
                return company.getAddress();
            default:
                return "";
        }
    }

    private String getFieldValueFromRequest(String fieldName, CompanyRequest companyRequest) {
        switch (fieldName) {
            case "company_name":
                return companyRequest.getCompanyName();
            case "address":
                return companyRequest.getAddress();
            default:
                return "";
        }
    }

    private boolean evaluateCondition(CompanyAutoApprovalRule rule, String fieldValue) {
        if (fieldValue == null) {
            fieldValue = "";
        }

        switch (rule.getOperator()) {
            case "NOT_EMPTY":
                return !fieldValue.trim().isEmpty();
            case "LENGTH_GREATER_THAN":
                try {
                    int minLength = Integer.parseInt(rule.getExpectedValue());
                    return fieldValue.length() > minLength;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "REGEX_MATCH":
                try {
                    Pattern pattern = Pattern.compile(rule.getExpectedValue());
                    return pattern.matcher(fieldValue).matches();
                } catch (Exception e) {
                    return false;
                }
            case "EQUALS":
                return fieldValue.equals(rule.getExpectedValue());
            case "CONTAINS":
                return fieldValue.toLowerCase().contains(rule.getExpectedValue().toLowerCase());
            default:
                return false;
        }
    }
}

