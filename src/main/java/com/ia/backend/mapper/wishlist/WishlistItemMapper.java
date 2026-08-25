package com.ia.backend.mapper.wishlist;

import com.ia.backend.dto.wishlist.items.WishlistItemResponse;
import com.ia.backend.entity.WishlistItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WishlistItemMapper {
    WishlistItemResponse toResponse(WishlistItem wishlistItem);
}