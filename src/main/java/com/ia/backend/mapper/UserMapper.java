package com.ia.backend.mapper;

import com.ia.backend.dto.user.UserRegisterRequest;
import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.dto.user.profile.UpdateProfileRequest;
import com.ia.backend.entity.User;
import com.ia.backend.entity.UserRole;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "email", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(UpdateProfileRequest request, @MappingTarget User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUser(UserRegisterRequest request);

    @Named("mapRoles")
    default Set<String> mapRoles(Set<UserRole> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(UserRole::getName)
                .collect(Collectors.toSet());
    }
}