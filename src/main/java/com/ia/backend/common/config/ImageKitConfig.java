package com.ia.backend.common.config;

import io.imagekit.client.ImageKitClient;
import io.imagekit.client.okhttp.ImageKitOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImageKitConfig {

    @Value("${imagekit.private-key}")
    private String privateKey;

    @Bean
    public ImageKitClient imageKitClient() {
        return ImageKitOkHttpClient.builder()
                .privateKey(privateKey)
                .build();
    }
}