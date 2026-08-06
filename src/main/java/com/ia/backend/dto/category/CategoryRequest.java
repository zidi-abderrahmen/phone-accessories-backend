package com.ia.backend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Name cannot be blank.")
        @Size(max = 50, message = "Name must not exceed 50 characters.")
        String name,

        @NotBlank(message = "Description cannot be blank.")
        @Size(max = 500, message = "Description must not exceed 500 characters.")
        String description,

        @NotBlank(message = "Image URL cannot be blank.")
        @Size(max = 500, message = "Image URL must not exceed 500 characters.")
        String imageUrl
) {}