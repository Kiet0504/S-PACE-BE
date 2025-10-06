package com.example.S_PACE.mapper;

import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.pojo.Company;
import com.example.S_PACE.pojo.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     */
    @Mapping(source = "user", target = "companyId", qualifiedByName = "extractCompanyId")
    UserResponse toUserResponse(User user);

    /**
     * Convert list of User entities to list of UserResponse DTOs
     */
    List<UserResponse> toUserResponseList(List<User> users);

    /**
     * Extract companyId from User entity, handling lazy loading
     */
    @Named("extractCompanyId")
    default UUID extractCompanyId(User user) {
        if (user == null) {
            return null;
        }

        Company company = user.getCompany();

        // Handle lazy-loaded company
        if (company != null && Hibernate.isInitialized(company)) {
            return company.getCompanyId();
        }

        // If company is not initialized (lazy), return null
        // This prevents LazyInitializationException
        return null;
    }
}
