package com.ia.backend.admin.dto;

import com.ia.backend.order.dto.OrderResponse;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardResponse(

        Long totalOrders,
        Long totalPendingOrders,
        BigDecimal totalRevenue,
        Long totalUsers,
        Long TotalAccessories,
        List<OrderResponse> lastOrders
) {}