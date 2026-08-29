package com.ia.backend.service;

import com.ia.backend.dto.admin.AdminDashboardResponse;
import com.ia.backend.dto.order.OrderResponse;
import com.ia.backend.entity.enums.OrderStatus;
import com.ia.backend.mapper.order.OrderMapper;
import com.ia.backend.repository.AccessoryRepository;
import com.ia.backend.repository.UserRepository;
import com.ia.backend.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AccessoryRepository accessoryRepository;

    private final OrderMapper orderMapper;

    protected Long getTotalOrders() {
        return orderRepository.count();
    }

    protected Long getTotalPendingOrders() {
        return orderRepository.countByStatus(OrderStatus.PENDING);
    }

    protected BigDecimal getTotalRevenue() {
        return orderRepository.getTotalRevenue(OrderStatus.CANCELLED);
    }

    protected Long getTotalUsers() {
        return userRepository.count();
    }

    protected Long getTotalAccessories() {
        return accessoryRepository.count();
    }

    protected List<OrderResponse> getLastOrders(int limit) {
        return orderRepository.getLastOrders(PageRequest.of(0, limit))
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard(int orderListLimit) {
        return new AdminDashboardResponse(
                getTotalOrders(),
                getTotalPendingOrders(),
                getTotalRevenue(),
                getTotalUsers(),
                getTotalAccessories(),
                getLastOrders(orderListLimit)
        );
    }
}