package com.ia.backend.integration;

import com.ia.backend.user.dto.login.UserLoginRequest;
import com.ia.backend.user.dto.register.UserRegisterRequest;
import com.ia.backend.user.verification.dto.VerifyEmailRequest;
import com.ia.backend.user.verification.service.EmailService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for full-stack integration tests.
 *
 * <p>Boots the real application context against the PostgreSQL {@code test} profile
 * (see {@code application-test.yaml}), with an in-process servlet layer driven by
 * {@link MockMvc}. Each test runs inside a transaction that is rolled back afterwards,
 * so tests are isolated without manual cleanup.</p>
 *
 * <p>The outbound Brevo {@link EmailService} is replaced by a Mockito mock: real
 * verification e-mails never leave the process, and the link (which is the only place
 * the raw verification token exists) is captured so tests can complete the flow.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

    protected static final String PASSWORD = "Password123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected EmailService emailService;

    @PersistenceContext
    private EntityManager entityManager;

    protected final List<SentEmail> sentEmails = new CopyOnWriteArrayList<>();

    protected record SentEmail(String recipient, String link, boolean resetPassword) {
    }

    @BeforeEach
    void resetCapturedEmails() {
        sentEmails.clear();
        doAnswer(invocation -> {
            sentEmails.add(new SentEmail(
                    invocation.getArgument(0),
                    invocation.getArgument(2),
                    invocation.getArgument(3)
            ));
            return null;
        }).when(emailService).sendEmail(anyString(), anyString(), anyString(), anyBoolean());
    }

    protected static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@test.example";
    }

    /**
     * Forces pending writes to the database and detaches everything from the shared
     * first-level cache. Because a single {@link Transactional} test spans several
     * MockMvc requests, entities loaded in one request stay cached for the next one;
     * clearing the persistence context makes each request observe exactly what its
     * own (fresh) persistence context would in production.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    protected void register(String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserRegisterRequest("Jane", "Doe", email, PASSWORD))))
                .andExpect(status().isCreated());
    }

    /**
     * Extracts the raw verification token from the link captured out of the mocked email.
     */
    protected String verificationToken(String email) {
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(capturedVerificationLink(email)).isNotNull());

        String link = capturedVerificationLink(email);
        return link.substring(link.indexOf("token=") + "token=".length());
    }

    protected void verifyEmail(String email) throws Exception {
        String token = verificationToken(email);
        mockMvc.perform(post("/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerifyEmailRequest(token))))
                .andExpect(status().isOk());
    }

    protected Session login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserLoginRequest(email, PASSWORD, false))))
                .andExpect(status().isOk())
                .andReturn();

        return Session.from(result.getResponse().getCookies());
    }

    protected Session registerVerifyAndLogin(String email) throws Exception {
        register(email);
        verifyEmail(email);
        return login(email);
    }

    private String capturedVerificationLink(String email) {
        return sentEmails.stream()
                .filter(sent -> sent.recipient().equals(email) && !sent.resetPassword())
                .map(SentEmail::link)
                .findFirst()
                .orElse(null);
    }

    /**
     * The authentication cookies produced by the login/refresh endpoints.
     */
    protected record Session(Cookie accessToken, Cookie refreshToken) {

        static Session from(Cookie[] cookies) {
            return new Session(cookie(cookies, "access_token"), cookie(cookies, "refresh_token"));
        }

        private static Cookie cookie(Cookie[] cookies, String name) {
            return Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing cookie: " + name));
        }
    }
}
