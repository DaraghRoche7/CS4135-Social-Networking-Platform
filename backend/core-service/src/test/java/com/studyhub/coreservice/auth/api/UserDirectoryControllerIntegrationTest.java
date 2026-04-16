package com.studyhub.coreservice.auth.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserDirectoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsUserDirectoryDataForTrustedServiceCaller() throws Exception {
        mockMvc.perform(get("/internal/users/{userId}", "demo-user")
                .header("X-Internal-Api-Key", "change-me-internal-api-key"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("demo-user"))
            .andExpect(jsonPath("$.email").value("student@studyhub.local"))
            .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void rejectsInternalRequestWithoutTrustedApiKey() throws Exception {
        mockMvc.perform(get("/internal/users/{userId}", "demo-user"))
            .andExpect(status().isUnauthorized());
    }
}
