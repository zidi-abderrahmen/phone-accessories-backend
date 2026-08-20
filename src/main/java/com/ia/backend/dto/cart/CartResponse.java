package com.ia.backend.dto.cart;

import com.ia.backend.dto.cart.item.CartItemResponse;
import com.ia.backend.dto.user.UserResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(

        Long id,
        UserResponse user,
        List<CartItemResponse> cartItems,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}