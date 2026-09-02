package com.ia.backend.order.dto;

import com.ia.backend.common.enums.OrderStatus;
import com.ia.backend.order.enums.PaymentMethod;
import com.ia.backend.order.enums.ShippingMethod;

import java.math.BigDecimal;
import java.util.Set;

public record OrderResponse(

        Long id,

        String customerFullName,
        String customerEmail,
        String customerPhoneNumber,

        String customerStreet,
        String customerCity,
        String customerPostalCode,
        String customerCountry,

        PaymentMethod paymentMethod,
        OrderStatus status,
        ShippingMethod shippingMethod,

        String notes,

        BigDecimal totalAmount,

        Set<OrderItemResponse> items
) {}