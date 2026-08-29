package com.ia.backend.order.dto;

import com.ia.backend.common.enums.PaymentMethod;
import com.ia.backend.common.enums.ShippingMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequest(

        @NotBlank(message = "Customer full name cannot be blank.")
        @Size(max = 300, message = "Customer full name must not exceed 300 characters.")
        String customerFullName,

        @Size(max = 150, message = "Customer email must not exceed 150 characters.")
        String customerEmail,

        @NotBlank(message = "Customer phone number cannot be blank.")
        @Size(max = 50, message = "Customer phone number must not exceed 50 characters.")
        String customerPhoneNumber,

        @NotBlank(message = "Customer street cannot be blank.")
        @Size(max = 50, message = "Customer street must not exceed 50 characters.")
        String customerStreet,

        @NotBlank(message = "Customer city cannot be blank.")
        @Size(max = 50, message = "Customer city must not exceed 50 characters.")
        String customerCity,

        @NotBlank(message = "Customer postal code cannot be blank.")
        @Size(max = 50, message = "Customer postal code must not exceed 50 characters.")
        String customerPostalCode,

        @NotBlank(message = "Customer country cannot be blank.")
        @Size(max = 50, message = "Customer country must not exceed 50 characters.")
        String customerCountry,

        @NotNull(message = "Payment method cannot be null.")
        PaymentMethod paymentMethod,

        @NotNull(message = "Shipping method cannot be null.")
        ShippingMethod shippingMethod,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters.")
        String notes
) {}