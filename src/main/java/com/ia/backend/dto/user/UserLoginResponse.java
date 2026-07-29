package com.ia.backend.dto.user;

public record UserLoginResponse(

        String accessToken,
        String refreshToken,
        UserResponse user
) {}