package com.ia.backend.integration;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Locks down the pagination contract enforced by
 * {@code com.ia.backend.common.web.ValidatingPageableArgumentResolver}: a default page
 * size when {@code size} is omitted, a hard cap, and a 400 for every invalid value.
 */
class PaginationIntegrationTest extends IntegrationTestBase {

    @Test
    void sizeOmitted_appliesDefaultPageSize() throws Exception {
        mockMvc.perform(get("/accessories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content.length()").value(lessThanOrEqualTo(20)));
    }

    @Test
    void sizeWithinCap_isHonoured() throws Exception {
        mockMvc.perform(get("/accessories").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.content.length()").value(lessThanOrEqualTo(5)));
    }

    @Test
    void sizeAboveCap_isRejected() throws Exception {
        mockMvc.perform(get("/accessories").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Pagination parameter 'size' must be between 1 and 100."))
                .andExpect(jsonPath("$.path").value("/accessories"));
    }

    @Test
    void sizeNotPositive_isRejected() throws Exception {
        mockMvc.perform(get("/accessories").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pagination parameter 'size' must be between 1 and 100."));

        mockMvc.perform(get("/accessories").param("size", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeNotNumeric_isRejected() throws Exception {
        mockMvc.perform(get("/accessories").param("size", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pagination parameter 'size' must be a valid integer."));
    }

    @Test
    void pageNegative_isRejected() throws Exception {
        mockMvc.perform(get("/accessories").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pagination parameter 'page' must be 0 or greater."));
    }

    @Test
    void pageNotNumeric_isRejected() throws Exception {
        mockMvc.perform(get("/accessories").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pagination parameter 'page' must be a valid integer."));
    }

    @Test
    void methodLevelPageableDefault_stillOverridesTheGlobalDefault() throws Exception {
        mockMvc.perform(get("/accessories/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(12));
    }

    @Test
    void cap_appliesAcrossPageableEndpoints() throws Exception {
        mockMvc.perform(get("/categories").param("size", "101"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/categories/{id}/accessories", 1L).param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}
