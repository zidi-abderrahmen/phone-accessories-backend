package com.ia.backend.dto.user.profile;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String email
) {}