package com.ia.backend.dto.user;

import com.ia.backend.dto.reftoken.RefreshTokenResponse;

public record UserLoginResponse(

        RefreshTokenResponse refreshToken,
        UserResponse user
) {}