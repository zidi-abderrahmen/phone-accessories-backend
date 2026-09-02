package com.ia.backend.cart.dto;

import jakarta.validation.constraints.NotNull;

public record CartItemRequest(

        @NotNull(message = "Accessory ID cannot be null.")
        Long accessoryId,

        @NotNull(message = "Quantity cannot be null.")
        int quantity

) {}