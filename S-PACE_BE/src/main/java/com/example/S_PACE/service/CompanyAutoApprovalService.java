package com.example.S_PACE.service;

import com.example.S_PACE.dto.request.CompanyRequest;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.pojo.CompanyAutoApprovalRule;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CompanyAutoApprovalService {

    /**
     * Tính điểm validation cho một công ty
     */
    Integer calculateValidationScore(Company company);

    /**
     * Tính điểm validation cho một company request
     */
    Integer calculateValidationScore(CompanyRequest companyRequest);

    /**
     * Kiểm tra xem công ty có đủ điều kiện để tự động duyệt không
     */
    boolean isEligibleForAutoApproval(Company company);

    /**
     * Kiểm tra xem company request có đủ điều kiện để tự động duyệt không
     */
    boolean isEligibleForAutoApproval(CompanyRequest companyRequest);

    /**
     * Lấy danh sách tất cả rules đang active
     */
    List<CompanyAutoApprovalRule> getActiveRules();

    /**
     * Lấy chi tiết validation cho một công ty
     */
    Map<String, Object> getValidationDetails(Company company);

    /**
     * Lấy chi tiết validation cho một company request
     */
    Map<String, Object> getValidationDetails(CompanyRequest companyRequest);

    /**
     * Cập nhật validation score cho một công ty
     */
    void updateValidationScore(UUID companyId);

    /**
     * Tạo rule mới
     */
    CompanyAutoApprovalRule createRule(CompanyAutoApprovalRule rule);

    /**
     * Cập nhật rule
     */
    CompanyAutoApprovalRule updateRule(UUID ruleId, CompanyAutoApprovalRule rule);

    /**
     * Xóa rule (soft delete)
     */
    void deleteRule(UUID ruleId);

    /**
     * Lấy rule theo ID
     */
    CompanyAutoApprovalRule getRuleById(UUID ruleId);

    /**
     * Lấy tất cả rules
     */
    List<CompanyAutoApprovalRule> getAllRules();

    /**
     * Tìm kiếm rules theo tên
     */
    List<CompanyAutoApprovalRule> searchRulesByName(String name);
}

