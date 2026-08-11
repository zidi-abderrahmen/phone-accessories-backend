package com.ia.backend.mapper;

import com.ia.backend.dto.user.role.UserRoleRequest;
import com.ia.backend.dto.user.role.UserRoleResponse;
import com.ia.backend.entity.UserRole;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    UserRoleResponse toDto(UserRole role);

    UserRole toEntity(UserRoleRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRole(UserRoleRequest request, @MappingTarget UserRole role);
}