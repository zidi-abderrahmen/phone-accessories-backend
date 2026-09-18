package com.ia.backend.service;

import com.ia.backend.common.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void testJwt() {
        String token = jwtUtils.generateTokenFromUsername("test@test.com");

        String email = jwtUtils.extractUsernameIfValid(token);

        assertThat(email).isEqualTo("test@test.com");
    }
}