package com.ia.backend.order.mapper;

import com.ia.backend.order.dto.OrderItemResponse;
import com.ia.backend.order.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItem orderItem);
}