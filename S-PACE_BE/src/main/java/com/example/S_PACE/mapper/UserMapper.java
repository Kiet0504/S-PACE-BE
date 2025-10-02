package com.example.S_PACE.mapper;

import com.example.S_PACE.dto.response.UserResponse;
import com.example.S_PACE.pojo.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    /**
     * Convert User entity to UserResponse DTO
     */
    @Mapping(source = "company.companyId", target = "companyId")
    UserResponse toUserResponse(User user);

    /**
     * Convert list of User entities to list of UserResponse DTOs
     */
    List<UserResponse> toUserResponseList(List<User> users);
}
