package com.sentineltrace.dto;

import com.sentineltrace.entity.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class EventIngestRequest {

    @NotNull(message = "eventType is required")
    private EventType eventType;

    @NotBlank(message = "sourceIp is required")
    @Size(max = 45, message = "sourceIp must not exceed 45 characters")
    @Pattern(
        regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){1,7}[0-9a-fA-F]{1,4}$|^localhost$",
        message = "sourceIp must be a valid IPv4 address, IPv6 address, or localhost"
    )
    private String sourceIp;

    @Size(max = 100, message = "username must not exceed 100 characters")
    private String username;

    @Size(max = 255, message = "endpoint must not exceed 255 characters")
    private String endpoint;

    @Size(max = 2000, message = "details must not exceed 2000 characters")
    private String details;

    private Instant occurredAt;

    public EventIngestRequest() {
    }

    public EventIngestRequest(EventType eventType, String sourceIp, String username, String endpoint, String details, Instant occurredAt) {
        this.eventType = eventType;
        this.sourceIp = sourceIp;
        this.username = username;
        this.endpoint = endpoint;
        this.details = details;
        this.occurredAt = occurredAt;
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
}
