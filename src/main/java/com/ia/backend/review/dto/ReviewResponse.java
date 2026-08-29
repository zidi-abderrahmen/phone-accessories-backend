package com.ia.backend.review.dto;

import com.ia.backend.accessory.dto.AccessoryResponse;
import com.ia.backend.user.dto.response.UserResponse;

import java.time.LocalDateTime;

public record ReviewResponse(

        Long id,
        UserResponse user,
        AccessoryResponse accessory,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean mine
) {}