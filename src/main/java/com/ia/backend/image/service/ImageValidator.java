package com.ia.backend.image.service;

import com.ia.backend.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ImageValidator {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BadRequestException("Invalid image file type, only JPEG, PNG, and WEBP are allowed");
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BadRequestException("Image file size exceeds the maximum allowed size of 5MB");
        }
    }
}