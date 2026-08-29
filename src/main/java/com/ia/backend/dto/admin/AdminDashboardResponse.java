package com.ia.backend.dto.admin;

import com.ia.backend.dto.order.OrderResponse;

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