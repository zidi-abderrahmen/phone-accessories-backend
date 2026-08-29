package com.ia.backend.password.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(message = "Token cannot be blank.")
        String token,

        @NotBlank(message = "Password cannot be blank.")
        @Size(min = 8, max = 150, message = "Password must be at least 8 characters long and 150 max.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
                message = "Password must contain at least one uppercase letter and one number"
        )
        String newPassword
) {}