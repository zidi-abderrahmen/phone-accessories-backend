package com.ia.backend.image.service;

import com.ia.backend.common.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

@Component
public class ImageValidator {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final long MAX_IMAGE_PIXELS = 25_000_000L; // 25 MP

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

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new BadRequestException("Invalid or corrupted image file");
            }

            long width = image.getWidth();
            long height = image.getHeight();

            long totalPixels = width * height;

            if (totalPixels > MAX_IMAGE_PIXELS) {
                throw new BadRequestException(
                        String.format(
                                "Image resolution exceeds the maximum allowed limit of 25 megapixels (%d × %d = %.2f MP)",
                                width,
                                height,
                                totalPixels / 1_000_000.0
                        )
                );
            }

        } catch (IOException e) {
            throw new BadRequestException("Unable to read image file");
        }
    }
}