package com.sentineltrace.dto;

import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;

import java.time.Instant;

public class EventResponse {

    private Long id;
    private EventType eventType;
    private String sourceIp;
    private String username;
    private String endpoint;
    private String details;
    private Instant occurredAt;
    private Instant createdAt;

    public EventResponse() {
    }

    public static EventResponse fromEntity(SecurityEvent event) {
        if (event == null) return null;
        EventResponse dto = new EventResponse();
        dto.setId(event.getId());
        dto.setEventType(event.getEventType());
        dto.setSourceIp(event.getSourceIp());
        dto.setUsername(event.getUsername());
        dto.setEndpoint(event.getEndpoint());
        dto.setDetails(event.getDetails());
        dto.setOccurredAt(event.getOccurredAt());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
