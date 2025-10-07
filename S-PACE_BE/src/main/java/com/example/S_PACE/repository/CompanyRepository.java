package com.example.S_PACE.repository;

import com.example.S_PACE.enums.CompanyStatus;
import com.example.S_PACE.pojo.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    
    // Find companies by status
    List<Company> findByStatus(CompanyStatus status);
    
    // Find companies by name (case-insensitive)
    @Query("SELECT c FROM Company c WHERE LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Company> findByCompanyNameContainingIgnoreCase(@Param("name") String name);
    
    // Find company by exact name (case-insensitive)
    @Query("SELECT c FROM Company c WHERE LOWER(c.companyName) = LOWER(:name)")
    Optional<Company> findByCompanyNameIgnoreCase(@Param("name") String name);
    
    // Find active companies only
    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE'")
    List<Company> findActiveCompanies();
    
    // Find companies that can create events
    @Query("SELECT c FROM Company c WHERE c.status = 'ACTIVE'")
    List<Company> findCompaniesThatCanCreateEvents();
}
