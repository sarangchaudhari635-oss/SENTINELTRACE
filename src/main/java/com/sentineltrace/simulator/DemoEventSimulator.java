package com.sentineltrace.simulator;

import com.sentineltrace.dto.EventIngestRequest;
import com.sentineltrace.dto.SimulationRequest;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.service.EventIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class DemoEventSimulator {

    private static final Logger log = LoggerFactory.getLogger(DemoEventSimulator.class);
    private final EventIngestionService ingestionService;

    public DemoEventSimulator(EventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Executes synthetic, safe security event simulations.
     * STRICT SAFETY GUARANTEE: Does not execute network scans or make external network calls.
     */
    public Map<String, Object> runSimulation(SimulationRequest request) {
        String scenario = request != null && request.getScenario() != null ?
                request.getScenario().toUpperCase() : "FAILED_LOGINS";
        int count = request != null && request.getCount() != null && request.getCount() > 0 ?
                Math.min(100, request.getCount()) : 0;
        String ip = request != null && request.getSourceIp() != null && !request.getSourceIp().isBlank() ?
                request.getSourceIp() : null;

        Map<String, Object> result = new LinkedHashMap<>();
        Instant now = Instant.now();

        switch (scenario) {
            case "FAILED_LOGINS" -> {
                int eventsToGenerate = count > 0 ? count : 5;
                String sourceIp = ip != null ? ip : "198.51.100.42";
                String username = request != null && request.getUsername() != null ? request.getUsername() : "sec_admin";

                log.info("Simulating brute force scenario: {} failed logins from {}", eventsToGenerate, sourceIp);
                for (int i = 0; i < eventsToGenerate; i++) {
                    Instant eventTime = now.minusSeconds((eventsToGenerate - i) * 6L);
                    EventIngestRequest req = new EventIngestRequest(
                            EventType.LOGIN_FAILURE,
                            sourceIp,
                            username,
                            "/api/auth/login",
                            "Simulated invalid password credential attempt #" + (i + 1),
                            eventTime
                    );
                    ingestionService.ingestEvent(req);
                }

                result.put("scenario", "FAILED_LOGINS");
                result.put("generatedCount", eventsToGenerate);
                result.put("sourceIp", sourceIp);
                result.put("message", "Generated " + eventsToGenerate + " synthetic LOGIN_FAILURE events from " + sourceIp);
            }
            case "HTTP_BURST" -> {
                int eventsToGenerate = count > 0 ? count : 32;
                String sourceIp = ip != null ? ip : "203.0.113.88";

                log.info("Simulating HTTP burst scenario: {} requests from {}", eventsToGenerate, sourceIp);
                for (int i = 0; i < eventsToGenerate; i++) {
                    Instant eventTime = now.minusSeconds((eventsToGenerate - i) * 1L);
                    EventIngestRequest req = new EventIngestRequest(
                            EventType.HTTP_REQUEST,
                            sourceIp,
                            "anonymous",
                            "/api/v1/search?q=test_" + i,
                            "Simulated rapid HTTP GET request iteration #" + (i + 1),
                            eventTime
                    );
                    ingestionService.ingestEvent(req);
                }

                result.put("scenario", "HTTP_BURST");
                result.put("generatedCount", eventsToGenerate);
                result.put("sourceIp", sourceIp);
                result.put("message", "Generated " + eventsToGenerate + " synthetic HTTP_REQUEST events from " + sourceIp);
            }
            case "NORMAL_TRAFFIC" -> {
                int eventsToGenerate = count > 0 ? count : 8;
                String[] demoIps = {"192.168.1.15", "192.168.1.20", "192.168.1.35"};
                String[] demoUsers = {"alice", "bob", "carol"};

                for (int i = 0; i < eventsToGenerate; i++) {
                    String sourceIp = demoIps[i % demoIps.length];
                    String username = demoUsers[i % demoUsers.length];
                    EventType type = (i % 3 == 0) ? EventType.LOGIN_SUCCESS : EventType.HTTP_REQUEST;
                    Instant eventTime = now.minusSeconds((eventsToGenerate - i) * 15L);

                    EventIngestRequest req = new EventIngestRequest(
                            type,
                            sourceIp,
                            username,
                            "/dashboard/home",
                            "Simulated normal user session activity",
                            eventTime
                    );
                    ingestionService.ingestEvent(req);
                }

                result.put("scenario", "NORMAL_TRAFFIC");
                result.put("generatedCount", eventsToGenerate);
                result.put("message", "Generated " + eventsToGenerate + " benign normal user traffic events");
            }
            default -> throw new IllegalArgumentException("Unknown simulation scenario: " + scenario + ". Supported: FAILED_LOGINS, HTTP_BURST, NORMAL_TRAFFIC");
        }

        return result;
    }
}
