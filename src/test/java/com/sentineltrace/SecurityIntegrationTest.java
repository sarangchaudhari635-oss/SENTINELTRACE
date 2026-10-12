package com.sentineltrace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Security: Unauthenticated access to protected management endpoint is denied (401)")
    void unauthenticatedAccessToProtectedManagementEndpointIsDenied() throws Exception {
        mockMvc.perform(get("/api/admin/system/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Security: Authenticated administrator access to management endpoint is permitted (200)")
    void authenticatedAdminAccessIsPermitted() throws Exception {
        mockMvc.perform(get("/api/admin/system/status")
                        .with(httpBasic("admin", "Admin#Sentinel2026")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Security: Public access to dashboard summary is permitted (200)")
    void publicAccessToDashboardSummaryIsPermitted() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk());
    }
}
