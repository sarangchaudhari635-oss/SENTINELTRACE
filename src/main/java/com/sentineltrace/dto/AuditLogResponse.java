package com.sentineltrace.dto;

import com.sentineltrace.entity.IncidentAuditLog;
import com.sentineltrace.entity.enums.IncidentStatus;

import java.time.Instant;

public class AuditLogResponse {

    private Long id;
    private Long incidentId;
    private String action;
    private IncidentStatus previousStatus;
    private IncidentStatus newStatus;
    private Instant createdAt;

    public AuditLogResponse() {
    }

    public static AuditLogResponse fromEntity(IncidentAuditLog log) {
        if (log == null) return null;
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(log.getId());
        if (log.getIncident() != null) {
            dto.setIncidentId(log.getIncident().getId());
        }
        dto.setAction(log.getAction());
        dto.setPreviousStatus(log.getPreviousStatus());
        dto.setNewStatus(log.getNewStatus());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(Long incidentId) {
        this.incidentId = incidentId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public IncidentStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(IncidentStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public IncidentStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(IncidentStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
