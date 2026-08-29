package com.ia.backend.wishlist.mapper;

import com.ia.backend.wishlist.dto.items.WishlistItemResponse;
import com.ia.backend.wishlist.entity.WishlistItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WishlistItemMapper {
    WishlistItemResponse toResponse(WishlistItem wishlistItem);
}