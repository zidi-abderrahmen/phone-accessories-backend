package com.ia.backend.cart.mapper;

import com.ia.backend.cart.dto.items.CartItemRequest;
import com.ia.backend.cart.dto.items.CartItemResponse;
import com.ia.backend.cart.entity.CartItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "accessory", target = "accessoryResponse")
    CartItemResponse toResponse(CartItem cartItem);

    @Mapping(target = "accessory", ignore = true)
    @Mapping(target = "cart", ignore = true)
    CartItem toEntity(CartItemRequest request);
}