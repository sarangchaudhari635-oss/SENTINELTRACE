package com.sentineltrace.entity;

import com.sentineltrace.entity.enums.EventType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "security_events", indexes = {
        @Index(name = "idx_events_type_ip_time", columnList = "event_type, source_ip, occurred_at"),
        @Index(name = "idx_events_time", columnList = "occurred_at")
})
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType;

    @Column(name = "source_ip", nullable = false, length = 45)
    private String sourceIp;

    @Column(length = 100)
    private String username;

    @Column(length = 255)
    private String endpoint;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SecurityEvent() {
    }

    public SecurityEvent(EventType eventType, String sourceIp, String username, String endpoint, String details, Instant occurredAt) {
        this.eventType = eventType;
        this.sourceIp = sourceIp;
        this.username = username;
        this.endpoint = endpoint;
        this.details = details;
        this.occurredAt = occurredAt != null ? occurredAt : Instant.now();
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.occurredAt == null) {
            this.occurredAt = Instant.now();
        }
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
