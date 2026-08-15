package com.ia.backend.dto.accessory;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccessoryRequest(

        @NotBlank(message = "Image URL cannot be blank.")
        @Size(max = 500, message = "Image URL must not exceed 500 characters.")
        String imageUrl,

        @NotBlank(message = "Title cannot be blank.")
        @Size(max = 150, message = "Title must not exceed 150 characters.")
        String title,

        @NotBlank(message = "Description cannot be blank.")
        @Size(max = 1000, message = "Description must not exceed 1000 characters.")
        String description,

        @NotNull(message = "Price cannot be null")
        @Positive(message = "Price cannot be negative")
        BigDecimal price,

        @NotNull(message = "Stock cannot be null")
        @PositiveOrZero(message = "Stock cannot be zero or negative")
        int stock,

        @NotNull(message = "Category cannot be null")
        Long categoryId,

        @NotBlank(message = "Product Code cannot be blank")
        @Size(max = 10, message = "Product Code must not exceed 10 characters")
        String productCode
) {}