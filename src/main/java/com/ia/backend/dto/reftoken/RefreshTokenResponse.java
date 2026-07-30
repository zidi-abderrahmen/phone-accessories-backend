package com.ia.backend.dto.reftoken;

public record RefreshTokenResponse(

        String accessToken,
        String refreshToken
) {}