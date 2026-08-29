package com.ia.backend.cart.dto.items;

import jakarta.validation.constraints.NotNull;

public record CartItemRequest(

        @NotNull(message = "Accessory ID cannot be null.")
        Long accessoryId,

        @NotNull(message = "Quantity cannot be null.")
        int quantity

) {}