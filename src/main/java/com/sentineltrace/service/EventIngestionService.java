package com.sentineltrace.service;

import com.sentineltrace.dto.BatchEventIngestRequest;
import com.sentineltrace.dto.EventIngestRequest;
import com.sentineltrace.dto.EventResponse;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final SecurityEventRepository eventRepository;
    private final DetectionEngine detectionEngine;

    public EventIngestionService(SecurityEventRepository eventRepository, DetectionEngine detectionEngine) {
        this.eventRepository = eventRepository;
        this.detectionEngine = detectionEngine;
    }

    /**
     * Ingests a single event, persists it in PostgreSQL, and immediately evaluates detection rules.
     */
    @Transactional
    public EventResponse ingestEvent(EventIngestRequest request) {
        Instant occurredAt = request.getOccurredAt() != null ? request.getOccurredAt() : Instant.now();
        String normalizedIp = request.getSourceIp().trim();
        String username = request.getUsername() != null ? request.getUsername().trim() : null;
        String endpoint = request.getEndpoint() != null ? request.getEndpoint().trim() : null;
        String details = request.getDetails() != null ? request.getDetails().trim() : null;

        SecurityEvent event = new SecurityEvent(
                request.getEventType(),
                normalizedIp,
                username,
                endpoint,
                details,
                occurredAt
        );

        SecurityEvent saved = eventRepository.saveAndFlush(event);
        log.debug("Persisted security event ID {} of type {} from {}", saved.getId(), saved.getEventType(), saved.getSourceIp());

        // Evaluate detection rules
        detectionEngine.evaluateEvent(saved);

        return EventResponse.fromEntity(saved);
    }

    /**
     * Ingests a batch of events within a transaction.
     */
    @Transactional
    public List<EventResponse> ingestBatch(BatchEventIngestRequest request) {
        List<EventResponse> results = new ArrayList<>();
        if (request == null || request.getEvents() == null) {
            return results;
        }

        for (EventIngestRequest singleRequest : request.getEvents()) {
            results.add(ingestEvent(singleRequest));
        }

        return results;
    }

    @Transactional(readOnly = true)
    public Page<EventResponse> listEvents(EventType eventType, String sourceIp, Instant from, Instant to, Pageable pageable) {
        return eventRepository.findWithFilters(eventType, sourceIp, from, to, pageable)
                .map(EventResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getRecentEvents(int limit) {
        return eventRepository.findTop15ByOrderByOccurredAtDesc()
                .stream()
                .limit(limit)
                .map(EventResponse::fromEntity)
                .toList();
    }
}
