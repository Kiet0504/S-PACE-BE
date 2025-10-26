package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.CompanyAutoApprovalRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyAutoApprovalRuleRepository extends JpaRepository<CompanyAutoApprovalRule, UUID> {

    /**
     * Tìm tất cả các rules đang active, sắp xếp theo priority giảm dần
     */
    @Query("SELECT r FROM CompanyAutoApprovalRule r WHERE r.isActive = true ORDER BY r.priority DESC")
    List<CompanyAutoApprovalRule> findActiveRulesOrderByPriority();

    /**
     * Tìm rules theo field name
     */
    List<CompanyAutoApprovalRule> findByFieldNameAndIsActive(String fieldName, Boolean isActive);

    /**
     * Tìm rules theo operator
     */
    List<CompanyAutoApprovalRule> findByOperatorAndIsActive(String operator, Boolean isActive);

    /**
     * Tìm rules theo priority range
     */
    @Query("SELECT r FROM CompanyAutoApprovalRule r WHERE r.priority BETWEEN :minPriority AND :maxPriority AND r.isActive = true ORDER BY r.priority DESC")
    List<CompanyAutoApprovalRule> findByPriorityRange(@Param("minPriority") Integer minPriority, @Param("maxPriority") Integer maxPriority);

    /**
     * Đếm số lượng rules đang active
     */
    @Query("SELECT COUNT(r) FROM CompanyAutoApprovalRule r WHERE r.isActive = true")
    Long countActiveRules();

    /**
     * Tìm rules theo tên (case insensitive)
     */
    @Query("SELECT r FROM CompanyAutoApprovalRule r WHERE LOWER(r.ruleName) LIKE LOWER(CONCAT('%', :name, '%')) AND r.isActive = true")
    List<CompanyAutoApprovalRule> findByRuleNameContainingIgnoreCase(@Param("name") String name);
}

