package com.ia.backend.user.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(

        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Set<String> roles,
        boolean enabled,
        boolean blocked,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {}