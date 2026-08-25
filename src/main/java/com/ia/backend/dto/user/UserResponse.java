package com.ia.backend.dto.user;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(

        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}