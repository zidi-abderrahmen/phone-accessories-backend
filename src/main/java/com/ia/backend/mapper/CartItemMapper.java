package com.ia.backend.mapper;

import com.ia.backend.dto.cart.item.CartItemRequest;
import com.ia.backend.dto.cart.item.CartItemResponse;
import com.ia.backend.entity.cart.CartItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "accessory", target = "accessoryResponse")
    CartItemResponse toResponse(CartItem cartItem);

    @Mapping(target = "accessory", ignore = true)
    @Mapping(target = "cart", ignore = true)
    CartItem toEntity(CartItemRequest request);
}