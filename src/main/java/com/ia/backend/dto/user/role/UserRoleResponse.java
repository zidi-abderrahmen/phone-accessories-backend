package com.ia.backend.dto.user.role;

import java.time.LocalDateTime;

public record UserRoleResponse(

        Long id,
        String name,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {}