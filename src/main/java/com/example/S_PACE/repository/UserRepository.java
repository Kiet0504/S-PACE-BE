package com.example.S_PACE.repository;

import com.example.S_PACE.enums.UserStatus;
import com.example.S_PACE.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.company.companyId = :company_id")
    List<User> findByCompanyId(@Param("company_id") UUID companyId);
    
    // New methods for UserController
    List<User> findByStatus(UserStatus status);
    List<User> findByRoleRoleName(String roleName);
}

