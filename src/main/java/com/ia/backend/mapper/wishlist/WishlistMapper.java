package com.ia.backend.mapper.wishlist;

import com.ia.backend.dto.wishlist.WishlistResponse;
import com.ia.backend.entity.Wishlist;
import com.ia.backend.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface WishlistMapper {
    WishlistResponse toResponse(Wishlist wishlist);
}