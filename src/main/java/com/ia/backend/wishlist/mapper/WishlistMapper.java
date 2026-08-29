package com.ia.backend.wishlist.mapper;

import com.ia.backend.wishlist.dto.WishlistResponse;
import com.ia.backend.wishlist.entity.Wishlist;
import com.ia.backend.user.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface WishlistMapper {
    WishlistResponse toResponse(Wishlist wishlist);
}