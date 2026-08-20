package com.ia.backend.mapper;

import com.ia.backend.entity.UserRole;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    default String map(UserRole role) {
        return role.getName();
    }
}