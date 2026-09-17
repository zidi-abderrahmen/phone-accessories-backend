package com.ia.backend.integration;

import com.ia.backend.user.dto.login.UserLoginRequest;
import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.verification.dto.VerifyEmailRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends IntegrationTestBase {

    @Test
    void register_createsDisabledUser_andSendsVerificationEmail() throws Exception {
        String email = uniqueEmail("register");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRegisterRequest("Jane", "Doe", email, PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.deleted").value(false))
                .andExpect(jsonPath("$.roles", hasItem("USER")));

        assertThat(verificationToken(email)).isNotBlank();
    }

    @Test
    void register_withDuplicateEmail_returns409() throws Exception {
        String email = uniqueEmail("duplicate");
        register(email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRegisterRequest("Jane", "Doe", email, PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_withWeakPassword_returns400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRegisterRequest("Jane", "Doe", uniqueEmail("weak"), "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/auth/register"))
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void register_withMalformedJson_returnsUnified400Envelope() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not-valid-json "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    void verifyEmail_withValidToken_enablesAccount() throws Exception {
        String email = uniqueEmail("verify");
        register(email);

        verifyEmail(email);

        login(email);
    }

    @Test
    void verifyEmail_withUnknownToken_returns404() throws Exception {
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VerifyEmailRequest("00000000-0000-0000-0000-000000000000"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_beforeEmailVerification_returns403() throws Exception {
        String email = uniqueEmail("unverified");
        register(email);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserLoginRequest(email, PASSWORD, false))))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_afterVerification_returns200WithAuthCookies() throws Exception {
        String email = uniqueEmail("login");
        register(email);
        verifyEmail(email);

        Session session = login(email);

        assertThat(session.accessToken().getValue()).isNotBlank();
        assertThat(session.refreshToken().getValue()).isNotBlank();
        assertThat(session.accessToken().isHttpOnly()).isTrue();
        assertThat(session.refreshToken().isHttpOnly()).isTrue();
        assertThat(session.accessToken().getPath()).isEqualTo("/");
        assertThat(session.accessToken().getValue()).isNotEqualTo(session.refreshToken().getValue());
    }

    @Test
    void accessProtectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpoint_withAccessToken_returnsCurrentUser() throws Exception {
        String email = uniqueEmail("me");
        Session session = registerVerifyAndLogin(email);

        mockMvc.perform(get("/profile/me").cookie(session.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }
}
