package com.ia.backend.dto.wishlist;

import com.ia.backend.dto.user.UserResponse;
import com.ia.backend.dto.wishlist.items.WishlistItemResponse;

import java.util.Set;

public record WishlistResponse(

        Long id,
        UserResponse user,
        Set<WishlistItemResponse> items
) {}