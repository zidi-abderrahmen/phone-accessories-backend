package com.ia.backend.mapper.order;

import com.ia.backend.dto.order.OrderRequest;
import com.ia.backend.dto.order.OrderResponse;
import com.ia.backend.entity.order.Order;
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