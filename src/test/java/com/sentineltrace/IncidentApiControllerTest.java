package com.sentineltrace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentineltrace.controller.IncidentApiController;
import com.sentineltrace.dto.IncidentDetailResponse;
import com.sentineltrace.dto.IncidentResponse;
import com.sentineltrace.dto.IncidentStatusUpdateRequest;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.exception.InvalidStateTransitionException;
import com.sentineltrace.exception.ResourceNotFoundException;
import com.sentineltrace.service.IncidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class IncidentApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncidentService incidentService;

    @Test
    @DisplayName("Requirement 8: Unknown incident ID returns HTTP 404")
    void unknownIncidentIdReturns404() throws Exception {
        when(incidentService.getIncidentById(999L))
                .thenThrow(new ResourceNotFoundException("Incident not found with ID: 999"));

        mockMvc.perform(get("/api/incidents/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Incident not found with ID: 999"));
    }

    @Test
    @DisplayName("Requirement 9: Invalid incident state transition returns HTTP 409 Conflict")
    void invalidStateTransitionReturns409() throws Exception {
        IncidentStatusUpdateRequest req = new IncidentStatusUpdateRequest(IncidentStatus.OPEN);

        when(incidentService.updateIncidentStatus(eq(1L), eq(IncidentStatus.OPEN)))
                .thenThrow(new InvalidStateTransitionException("Cannot transition incident 1 from RESOLVED to OPEN"));

        mockMvc.perform(patch("/api/incidents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("Valid status transition returns HTTP 200 OK")
    void validStatusTransitionReturns200() throws Exception {
        IncidentStatusUpdateRequest req = new IncidentStatusUpdateRequest(IncidentStatus.ACKNOWLEDGED);

        IncidentResponse updated = new IncidentResponse();
        updated.setId(1L);
        updated.setStatus(IncidentStatus.ACKNOWLEDGED);
        updated.setSeverity(Severity.HIGH);

        when(incidentService.updateIncidentStatus(eq(1L), eq(IncidentStatus.ACKNOWLEDGED)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/incidents/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));
    }
}
