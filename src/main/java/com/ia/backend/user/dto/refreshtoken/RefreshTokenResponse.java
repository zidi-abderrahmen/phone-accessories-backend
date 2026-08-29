package com.ia.backend.user.dto.refreshtoken;

public record RefreshTokenResponse(

        String accessToken,
        String refreshToken,
        boolean rememberMe
) {}