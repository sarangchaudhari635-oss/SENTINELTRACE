package com.sentineltrace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentineltrace.controller.EventApiController;
import com.sentineltrace.dto.BatchEventIngestRequest;
import com.sentineltrace.dto.EventIngestRequest;
import com.sentineltrace.dto.EventResponse;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.service.EventIngestionService;
import com.sentineltrace.simulator.DemoEventSimulator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventApiController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filter chain for isolated controller unit testing
class EventApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventIngestionService ingestionService;

    @MockBean
    private DemoEventSimulator simulator;

    @Test
    @DisplayName("Requirement 7: Valid input returns HTTP 201")
    void validEventReturns201Created() throws Exception {
        EventIngestRequest request = new EventIngestRequest(
                EventType.LOGIN_FAILURE,
                "192.168.1.100",
                "admin",
                "/login",
                "Invalid credentials",
                Instant.now()
        );

        EventResponse response = new EventResponse();
        response.setId(1L);
        response.setEventType(EventType.LOGIN_FAILURE);
        response.setSourceIp("192.168.1.100");

        when(ingestionService.ingestEvent(any(EventIngestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventType").value("LOGIN_FAILURE"))
                .andExpect(jsonPath("$.sourceIp").value("192.168.1.100"));
    }

    @Test
    @DisplayName("Requirement 7: Missing eventType returns HTTP 400 Bad Request")
    void missingEventTypeReturns400() throws Exception {
        String invalidPayload = """
                {
                    "sourceIp": "192.168.1.100",
                    "username": "admin"
                }
                """;

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Requirement 7: Invalid source IP returns HTTP 400 Bad Request")
    void invalidSourceIpReturns400() throws Exception {
        String invalidPayload = """
                {
                    "eventType": "LOGIN_FAILURE",
                    "sourceIp": "999.999.999.999",
                    "username": "admin"
                }
                """;

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Requirement 7: Batch event ingestion returns HTTP 201 Created")
    void batchIngestReturns201Created() throws Exception {
        EventIngestRequest event1 = new EventIngestRequest(EventType.LOGIN_FAILURE, "10.0.0.1", "u1", "/l", "fail", Instant.now());
        EventIngestRequest event2 = new EventIngestRequest(EventType.LOGIN_FAILURE, "10.0.0.1", "u1", "/l", "fail", Instant.now());
        BatchEventIngestRequest batch = new BatchEventIngestRequest(List.of(event1, event2));

        EventResponse r1 = new EventResponse();
        r1.setId(1L);
        EventResponse r2 = new EventResponse();
        r2.setId(2L);

        when(ingestionService.ingestBatch(any(BatchEventIngestRequest.class))).thenReturn(List.of(r1, r2));

        mockMvc.perform(post("/api/events/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batch)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
