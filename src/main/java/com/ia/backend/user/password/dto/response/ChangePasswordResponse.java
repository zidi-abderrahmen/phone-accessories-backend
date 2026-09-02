package com.ia.backend.user.password.dto.response;

import com.ia.backend.user.dto.response.UserResponse;

public record ChangePasswordResponse(

        UserResponse user,
        String message
) {}