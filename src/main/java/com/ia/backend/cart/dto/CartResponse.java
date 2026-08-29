package com.ia.backend.cart.dto;

import com.ia.backend.cart.dto.items.CartItemResponse;
import com.ia.backend.user.dto.response.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(

        Long id,
        UserResponse user,
        List<CartItemResponse> cartItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}