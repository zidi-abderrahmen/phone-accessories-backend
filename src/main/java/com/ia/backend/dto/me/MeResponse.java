package com.ia.backend.dto.me;

import java.time.LocalDateTime;
import java.util.Set;

public record MeResponse(

        String id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}