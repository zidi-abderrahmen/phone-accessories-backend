package com.ia.backend.wishlist.dto;

import com.ia.backend.user.dto.response.UserResponse;
import com.ia.backend.wishlist.dto.items.WishlistItemResponse;

import java.util.Set;

public record WishlistResponse(

        Long id,
        UserResponse user,
        Set<WishlistItemResponse> items
) {}