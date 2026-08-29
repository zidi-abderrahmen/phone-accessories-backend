package com.ia.backend.user.dto.login;

import com.ia.backend.user.dto.refreshtoken.RefreshTokenResponse;
import com.ia.backend.user.dto.response.UserResponse;

public record UserLoginResponse(

        RefreshTokenResponse tokens,
        UserResponse user
) {}