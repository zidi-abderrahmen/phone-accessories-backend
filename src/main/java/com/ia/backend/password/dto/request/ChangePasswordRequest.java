package com.ia.backend.password.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "Old password cannot be blank.")
        @Size(min = 8, max = 150, message = "Old password must be at least 8 characters long and 150 max.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
                message = "Old password must contain at least one uppercase letter and one number"
        )
        String oldPassword,

        @NotBlank(message = "New password cannot be blank.")
        @Size(min = 8, max = 150, message = "New password must be at least 8 characters long and 150 max.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
                message = "New password must contain at least one uppercase letter and one number"
        )
        String newPassword
) {}