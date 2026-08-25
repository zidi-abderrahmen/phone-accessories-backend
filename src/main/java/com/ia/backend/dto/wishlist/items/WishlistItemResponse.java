package com.ia.backend.dto.wishlist.items;

import com.ia.backend.dto.accessory.AccessoryResponse;

import java.time.LocalDateTime;

public record WishlistItemResponse(

        Long id,
        AccessoryResponse accessory,
        LocalDateTime createdAt
) {}