package com.ia.backend.dto.accessory;

import com.ia.backend.entity.enums.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccessoryResponse(

        Long id,
        String title,
        String description,
        BigDecimal price,
        int stock,
        Category category,
        String productCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}