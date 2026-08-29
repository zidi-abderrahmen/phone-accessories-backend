package com.ia.backend.user.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRoleRequest(

        @NotBlank(message = "Name cannot be blank.")
        @Size(max = 50, message = "Name must not exceed 50 characters.")
        String name,

        @NotNull(message = "Deleted cannot be null.")
        boolean deleted
) {}