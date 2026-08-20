package com.ia.backend.dto.cart.item;

import com.ia.backend.dto.accessory.AccessoryResponse;

public record CartItemResponse(

        Long id,
        AccessoryResponse accessoryResponse,
        int quantity
) {}