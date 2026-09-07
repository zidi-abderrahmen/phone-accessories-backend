package com.ia.backend.image.service;

import com.ia.backend.common.exception.BadRequestException;
import com.ia.backend.image.dto.ImageUploadResponse;
import io.imagekit.client.ImageKitClient;
import io.imagekit.errors.ImageKitException;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final ImageKitClient imageKitClient;
    private final ImageValidator imageValidator;

    @Transactional
    public ImageUploadResponse uploadImage(MultipartFile file, String folderPath) {
        imageValidator.validateImage(file);

        try {
            byte[] imageData = file.getBytes();
            String safeFileName = generateSafeFileName(file.getOriginalFilename());

            FileUploadParams params = FileUploadParams.builder()
                    .file(imageData)
                    .fileName(safeFileName)
                    .folder(folderPath)
                    .build();

            FileUploadResponse response = imageKitClient.files().upload(params);

            return new ImageUploadResponse(
                    response.url(),
                    response.fileId(),
                    response.name()
            );

        } catch (IOException e) {
            log.error("Error reading file", e);
            throw new BadRequestException("Error reading file");
        } catch (ImageKitException e) {
            log.error("Error uploading image to ImageKit", e);
            throw new BadRequestException("Error uploading image");
        }
    }

    private String generateSafeFileName(String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        return UUID.randomUUID() + extension;
    }
}