package com.ia.backend.dto.review;

import com.ia.backend.dto.accessory.AccessoryResponse;
import com.ia.backend.dto.user.UserResponse;

import java.time.LocalDateTime;

public record ReviewResponse(

        Long id,
        UserResponse user,
        AccessoryResponse accessory,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}