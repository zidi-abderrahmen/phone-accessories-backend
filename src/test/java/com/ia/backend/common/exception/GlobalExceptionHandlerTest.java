package com.ia.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void uncaughtExceptions_becomeOpaque500Envelope_withoutLeakingDetails() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/orders/42");
        RuntimeException boom = new RuntimeException("jdbc:postgresql://db user=abdo");

        ResponseEntity<ApiErrorResponse> response = handler.handleUncaughtException(boom, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.error()).isEqualTo("Internal Server Error");
        assertThat(body.message()).doesNotContain("jdbc", "abdo");
        assertThat(body.path()).isEqualTo("/orders/42");
        assertThat(body.timestamp()).isNotNull();
    }

    @Test
    void domainExceptions_keepTheirStatusMessageAndPath() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/accessories/99");

        ResponseEntity<ApiErrorResponse> response = handler.handleNotFoundException(
                new NotFoundException("Accessory not found."), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("Accessory not found.");
        assertThat(response.getBody().path()).isEqualTo("/accessories/99");
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }
}
