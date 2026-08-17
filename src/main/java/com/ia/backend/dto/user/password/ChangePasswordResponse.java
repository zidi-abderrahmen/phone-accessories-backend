package com.ia.backend.dto.user.password;

import com.ia.backend.dto.user.UserResponse;

public record ChangePasswordResponse(

        UserResponse user,
        String message
) {}