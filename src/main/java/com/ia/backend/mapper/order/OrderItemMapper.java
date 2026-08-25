package com.ia.backend.mapper.order;

import com.ia.backend.dto.order.items.OrderItemResponse;
import com.ia.backend.entity.order.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItem orderItem);
}