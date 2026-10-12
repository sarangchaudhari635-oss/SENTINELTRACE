package com.sentineltrace.dto;

import java.util.List;

public class IncidentDetailResponse {

    private IncidentResponse incident;
    private DetectionRuleResponse rule;
    private List<EventResponse> evidenceEvents;
    private List<AuditLogResponse> auditLogs;

    public IncidentDetailResponse() {
    }

    public IncidentDetailResponse(IncidentResponse incident, DetectionRuleResponse rule, List<EventResponse> evidenceEvents, List<AuditLogResponse> auditLogs) {
        this.incident = incident;
        this.rule = rule;
        this.evidenceEvents = evidenceEvents;
        this.auditLogs = auditLogs;
    }

    public IncidentResponse getIncident() {
        return incident;
    }

    public void setIncident(IncidentResponse incident) {
        this.incident = incident;
    }

    public DetectionRuleResponse getRule() {
        return rule;
    }

    public void setRule(DetectionRuleResponse rule) {
        this.rule = rule;
    }

    public List<EventResponse> getEvidenceEvents() {
        return evidenceEvents;
    }

    public void setEvidenceEvents(List<EventResponse> evidenceEvents) {
        this.evidenceEvents = evidenceEvents;
    }

    public List<AuditLogResponse> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLogResponse> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
