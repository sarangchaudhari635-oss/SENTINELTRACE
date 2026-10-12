package com.sentineltrace.dto;

import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;

import java.time.Instant;

public class IncidentResponse {

    private Long id;
    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String fingerprint;
    private String sourceIp;
    private String title;
    private Severity severity;
    private Integer riskScore;
    private IncidentStatus status;
    private Instant firstSeen;
    private Instant lastSeen;
    private Integer evidenceCount;
    private Instant createdAt;
    private Instant resolvedAt;

    public IncidentResponse() {
    }

    public static IncidentResponse fromEntity(Incident incident) {
        if (incident == null) return null;
        IncidentResponse dto = new IncidentResponse();
        dto.setId(incident.getId());
        if (incident.getRule() != null) {
            dto.setRuleId(incident.getRule().getId());
            dto.setRuleCode(incident.getRule().getRuleCode());
            dto.setRuleName(incident.getRule().getName());
        }
        dto.setFingerprint(incident.getFingerprint());
        dto.setSourceIp(incident.getSourceIp());
        dto.setTitle(incident.getTitle());
        dto.setSeverity(incident.getSeverity());
        dto.setRiskScore(incident.getRiskScore());
        dto.setStatus(incident.getStatus());
        dto.setFirstSeen(incident.getFirstSeen());
        dto.setLastSeen(incident.getLastSeen());
        dto.setEvidenceCount(incident.getEvidenceCount());
        dto.setCreatedAt(incident.getCreatedAt());
        dto.setResolvedAt(incident.getResolvedAt());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public void setFirstSeen(Instant firstSeen) {
        this.firstSeen = firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Integer getEvidenceCount() {
        return evidenceCount;
    }

    public void setEvidenceCount(Integer evidenceCount) {
        this.evidenceCount = evidenceCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
