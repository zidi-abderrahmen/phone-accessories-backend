package com.ia.backend.dto.verifemail;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(

        @NotBlank(message = "Token cannot be blank.")
        String token
) {}