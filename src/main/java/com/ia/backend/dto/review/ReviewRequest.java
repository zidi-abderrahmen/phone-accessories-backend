package com.ia.backend.dto.review;

import jakarta.validation.constraints.*;

public record ReviewRequest(

        @NotNull(message = "Rating cannot be null.")
        @Min(1) @Max(5)
        Integer rating,

        @NotBlank(message = "Comment cannot be blank.")
        @Size(max = 2000, message = "Comment must not exceed 2000 characters.")
        String comment
) {}