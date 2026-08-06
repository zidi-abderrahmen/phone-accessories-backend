package com.ia.backend.dto.accessory;

import com.ia.backend.dto.category.CategoryResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccessoryResponse(

        Long id,
        String title,
        String description,
        BigDecimal price,
        int stock,
        CategoryResponse category,
        String productCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}