package com.ia.backend.accessory.dto;

import com.ia.backend.category.dto.CategoryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccessoryResponse(

        Long id,
        String imageUrl,
        String title,
        String description,
        BigDecimal price,
        int stock,
        CategoryResponse category,
        String productCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}