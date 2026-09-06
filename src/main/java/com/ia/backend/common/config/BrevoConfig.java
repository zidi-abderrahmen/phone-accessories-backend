package com.ia.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class BrevoConfig {

    @Bean
    public RestClient brevoRestClient(
            @Value("${app.mail.api-key}") String apiKey
    ) {
        return RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", apiKey)
                .build();
    }
}