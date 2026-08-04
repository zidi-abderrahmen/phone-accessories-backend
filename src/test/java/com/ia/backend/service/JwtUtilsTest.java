package com.ia.backend.service;

import com.ia.backend.util.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

@SpringBootTest
public class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void testJwt() {
        String token = jwtUtils.generateTokenFromUsername("test@test.com");

        boolean valid = jwtUtils.validateJwtToken(token);

        String email = jwtUtils.getUsernameFromJwtToken(token);
    }
}