package com.ia.backend.accessory.dto;

import java.math.BigDecimal;

public record SearchRequest(

        Long categoryId,
        String keyword,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean inStock
) {}