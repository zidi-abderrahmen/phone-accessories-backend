package com.ia.backend.cart.mapper;

import com.ia.backend.user.entity.UserRole;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    default String map(UserRole role) {
        return role.getName();
    }
}