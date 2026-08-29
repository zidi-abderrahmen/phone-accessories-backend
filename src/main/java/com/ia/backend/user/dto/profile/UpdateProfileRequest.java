package com.ia.backend.user.dto.profile;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String email
) {}