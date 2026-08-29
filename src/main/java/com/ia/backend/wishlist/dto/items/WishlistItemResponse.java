package com.ia.backend.wishlist.dto.items;

import com.ia.backend.accessory.dto.AccessoryResponse;

import java.time.LocalDateTime;

public record WishlistItemResponse(

        Long id,
        AccessoryResponse accessory,
        LocalDateTime createdAt
) {}