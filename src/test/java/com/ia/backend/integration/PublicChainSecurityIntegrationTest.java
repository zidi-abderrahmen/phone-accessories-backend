package com.ia.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Locks down the semantics of the public security chain: the anonymous surface is exactly
 * the auth endpoints plus HTTP {@code GET} on the catalog paths. Any other request routed
 * to that chain must be authenticated, and role checks (e.g. admin-only writes) still
 * apply on top of it.
 */
class PublicChainSecurityIntegrationTest extends IntegrationTestBase {

    private static final String CATEGORY_BODY = """
            {"name":"Security Test","description":"Integration test category",
             "imageUrl":"https://example.com/category.png"}
            """;

    @Test
    void publicCatalogReads_areAllowedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/accessories")).andExpect(status().isOk());
    }

    @Test
    void publicCatalogWrites_withoutToken_areUnauthorized() throws Exception {
        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORY_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicCatalogWrites_withNonAdminToken_areForbidden() throws Exception {
        Session session = registerVerifyAndLogin(uniqueEmail("non-admin"));

        mockMvc.perform(post("/categories")
                        .cookie(session.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CATEGORY_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointsOutsideThePublicChain_stillRequireAuthentication() throws Exception {
        mockMvc.perform(get("/orders")).andExpect(status().isUnauthorized());
    }
}
