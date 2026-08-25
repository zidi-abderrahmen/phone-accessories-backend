package com.ia.backend.repository.order;

import com.ia.backend.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Set<OrderItem> findAllByOrder_Id(Long orderId);
    Optional<OrderItem> findByOrder_IdAndAccessory_Id(Long orderId, Long accessoryId);
    void deleteAllByOrder_Id(Long orderId);
}