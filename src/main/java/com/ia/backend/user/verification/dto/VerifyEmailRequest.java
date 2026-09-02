package com.ia.backend.user.verification.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank(message = "Token cannot be blank.")
        String token
) {}