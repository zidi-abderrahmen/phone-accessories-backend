package com.ia.backend.category.dto;

import java.time.LocalDateTime;

public record CategoryResponse(

        Long id,
        String name,
        String description,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}