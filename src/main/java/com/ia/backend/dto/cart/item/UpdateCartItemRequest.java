package com.ia.backend.dto.cart.item;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(

        @NotNull(message = "New quantity cannot be null.")
        @Positive(message = "New quantity must be positive.")
        int newQuantity
) {}