package com.ia.backend.dto.reftoken;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token cannot be blank.")
        String refreshToken
) {}