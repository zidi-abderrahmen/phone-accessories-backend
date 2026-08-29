package com.ia.backend.user.mapper;

import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.dto.response.UserResponse;
import com.ia.backend.user.dto.profile.UpdateProfileRequest;
import com.ia.backend.user.entity.User;
import com.ia.backend.user.entity.UserRole;
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