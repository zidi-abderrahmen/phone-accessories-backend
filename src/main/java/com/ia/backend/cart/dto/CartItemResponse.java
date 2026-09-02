package com.ia.backend.cart.dto;

import com.ia.backend.accessory.dto.AccessoryResponse;

public record CartItemResponse(

        Long id,
        AccessoryResponse accessoryResponse,
        int quantity
) {}