package com.ia.backend.dto.order.items;

import com.ia.backend.dto.accessory.AccessoryResponse;

import java.math.BigDecimal;

public record OrderItemResponse(

        Long id,
        AccessoryResponse accessory,
        Integer quantity,
        BigDecimal unitPrice
) {}