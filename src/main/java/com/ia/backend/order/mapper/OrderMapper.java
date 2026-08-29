package com.ia.backend.order.mapper;

import com.ia.backend.order.dto.OrderRequest;
import com.ia.backend.order.dto.OrderResponse;
import com.ia.backend.order.entity.Order;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponses(List<Order> orders);

    @Mapping(target = "items", ignore = true)
    @Mapping(target = "user", ignore = true)
    Order toEntity(OrderRequest request);
}