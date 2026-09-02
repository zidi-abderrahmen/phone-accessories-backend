package com.ia.backend.order.dto;

import com.ia.backend.accessory.dto.AccessoryResponse;

import java.math.BigDecimal;

public record OrderItemResponse(

        Long id,
        AccessoryResponse accessory,
        Integer quantity,
        BigDecimal unitPrice
) {}