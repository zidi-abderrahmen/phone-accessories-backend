package com.ia.backend.image.dto;

import java.util.Optional;

public record ImageUploadResponse(
        Optional<String> url,
        Optional<String> fileId,
        Optional<String> fileName
) {}