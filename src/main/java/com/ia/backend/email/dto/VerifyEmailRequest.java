package com.ia.backend.email.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank(message = "Token cannot be blank.")
        String token
) {}