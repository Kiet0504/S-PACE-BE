package com.example.S_PACE.mapper;

import com.example.S_PACE.dto.response.RoleResponse;
import com.example.S_PACE.pojo.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    /**
     * Convert Role entity to RoleResponse DTO
     */
    RoleResponse toRoleResponse(Role role);

    /**
     * Convert list of Role entities to list of RoleResponse DTOs
     */
    List<RoleResponse> toRoleResponseList(List<Role> roles);
}
