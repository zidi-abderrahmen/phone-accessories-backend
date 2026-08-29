package com.ia.backend.user.dto.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

        @NotBlank(message = "First name cannot be blank.")
        @Size(max = 150, message = "First name must not exceed 150 characters.")
        String firstName,

        @NotBlank(message = "Last name cannot be blank.")
        @Size(max = 150, message = "Last name must not exceed 150 characters.")
        String lastName,

        @NotBlank(message = "Email cannot be blank.")
        @Email(message = "Invalid email format.")
        @Size(max = 150, message = "Email must not exceed 150 characters.")
        String email,

        @NotBlank(message = "Password cannot be blank.")
        @Size(min = 8, max = 150, message = "Password must be at least 8 characters long and 150 max.")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9]).*$",
                message = "Password must contain at least one uppercase letter and one number"
        )
        String password
) {}