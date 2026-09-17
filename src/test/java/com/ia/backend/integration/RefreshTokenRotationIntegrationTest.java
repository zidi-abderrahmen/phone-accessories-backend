package com.ia.backend.integration;

import com.ia.backend.user.dto.login.UserLoginRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefreshTokenRotationIntegrationTest extends IntegrationTestBase {

    @Test
    void refreshToken_rotatesToken_andInvalidatesOldOne() throws Exception {
        Session session = registerVerifyAndLogin(uniqueEmail("rotation"));

        Session rotated = refresh(session.refreshToken());

        assertThat(rotated.refreshToken().getValue()).isNotEqualTo(session.refreshToken().getValue());
        assertThat(rotated.accessToken().getValue()).isNotBlank();

        mockMvc.perform(post("/auth/refresh-token").cookie(session.refreshToken()))
                .andExpect(status().isNotFound());

        refresh(rotated.refreshToken());
    }

    @Test
    void refreshedAccessToken_canAccessProtectedEndpoint() throws Exception {
        Session session = registerVerifyAndLogin(uniqueEmail("refreshed-access"));

        Session rotated = refresh(session.refreshToken());

        mockMvc.perform(get("/profile/me").cookie(rotated.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void refreshToken_withoutCookie_returns401() throws Exception {
        mockMvc.perform(post("/auth/refresh-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_withUnknownToken_returns404() throws Exception {
        mockMvc.perform(post("/auth/refresh-token")
                        .cookie(new Cookie("refresh_token", "not-a-real-refresh-token")))
                .andExpect(status().isNotFound());
    }

    @Test
    void logout_revokesRefreshToken() throws Exception {
        Session session = registerVerifyAndLogin(uniqueEmail("logout"));

        mockMvc.perform(post("/auth/logout")
                        .cookie(session.accessToken(), session.refreshToken()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/auth/refresh-token").cookie(session.refreshToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void refreshToken_preservesRememberMe() throws Exception {
        String email = uniqueEmail("remember-me");
        register(email);
        verifyEmail(email);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserLoginRequest(email, PASSWORD, true))))
                .andExpect(status().isOk())
                .andReturn();

        Session session = Session.from(loginResult.getResponse().getCookies());
        Session rotated = refresh(session.refreshToken());

        // remember-me refresh tokens are issued with the long-lived expiration
        assertThat(rotated.refreshToken().getMaxAge()).isEqualTo(2_592_000);
    }

    private Session refresh(Cookie refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/refresh-token").cookie(refreshToken))
                .andExpect(status().isNoContent())
                .andReturn();

        return Session.from(result.getResponse().getCookies());
    }
}
