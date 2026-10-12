package com.sentineltrace.controller;

import com.sentineltrace.dto.*;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.service.EventIngestionService;
import com.sentineltrace.simulator.DemoEventSimulator;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EventApiController {

    private final EventIngestionService ingestionService;
    private final DemoEventSimulator simulator;

    public EventApiController(EventIngestionService ingestionService, DemoEventSimulator simulator) {
        this.ingestionService = ingestionService;
        this.simulator = simulator;
    }

    @PostMapping("/events")
    public ResponseEntity<EventResponse> ingestEvent(@Valid @RequestBody EventIngestRequest request) {
        EventResponse response = ingestionService.ingestEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/events")
    public ResponseEntity<PageResponse<EventResponse>> listEvents(
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) String sourceIp,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Enforce maximum page size to prevent resource exhaustion
        int boundedSize = Math.min(Math.max(1, size), 100);
        int boundedPage = Math.max(0, page);
        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "occurredAt"));
        PageResponse<EventResponse> response = PageResponse.of(ingestionService.listEvents(eventType, sourceIp, from, to, pageable));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/events/batch")
    public ResponseEntity<List<EventResponse>> ingestBatch(@Valid @RequestBody BatchEventIngestRequest request) {
        List<EventResponse> responses = ingestionService.ingestBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @PostMapping("/demo/simulate")
    public ResponseEntity<Map<String, Object>> runDemoSimulation(@RequestBody(required = false) SimulationRequest request) {
        Map<String, Object> result = simulator.runSimulation(request);
        return ResponseEntity.ok(result);
    }
}
