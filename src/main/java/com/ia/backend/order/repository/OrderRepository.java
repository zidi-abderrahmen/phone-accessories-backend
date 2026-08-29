package com.ia.backend.order.repository;

import com.ia.backend.common.enums.OrderStatus;
import com.ia.backend.order.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.user.id = :userId")
    List<Order> findAllByUserIdWithItems(@Param("userId") String userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") String userId);

    Long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != :status")
    BigDecimal getTotalRevenue(@Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o ORDER BY o.id DESC")
    List<Order> getLastOrders(Pageable pageable);
}