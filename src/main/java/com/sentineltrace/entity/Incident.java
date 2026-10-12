package com.sentineltrace.entity;

import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incidents_status_severity_time", columnList = "status, severity, last_seen"),
        @Index(name = "idx_incidents_fingerprint_status", columnList = "fingerprint, status")
})
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private DetectionRule rule;

    @Column(nullable = false, length = 180)
    private String fingerprint;

    @Column(name = "source_ip", nullable = false, length = 45)
    private String sourceIp;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "first_seen", nullable = false)
    private Instant firstSeen;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    @Column(name = "evidence_count", nullable = false)
    private Integer evidenceCount = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public Incident() {
    }

    public Incident(DetectionRule rule, String fingerprint, String sourceIp, String title, Severity severity, Integer riskScore, IncidentStatus status, Instant firstSeen, Instant lastSeen, Integer evidenceCount) {
        this.rule = rule;
        this.fingerprint = fingerprint;
        this.sourceIp = sourceIp;
        this.title = title;
        this.severity = severity;
        this.riskScore = Math.max(0, Math.min(100, riskScore != null ? riskScore : 50));
        this.status = status != null ? status : IncidentStatus.OPEN;
        this.firstSeen = firstSeen != null ? firstSeen : Instant.now();
        this.lastSeen = lastSeen != null ? lastSeen : this.firstSeen;
        this.evidenceCount = evidenceCount != null ? evidenceCount : 1;
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.riskScore == null) {
            this.riskScore = 50;
        } else {
            this.riskScore = Math.max(0, Math.min(100, this.riskScore));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DetectionRule getRule() {
        return rule;
    }

    public void setRule(DetectionRule rule) {
        this.rule = rule;
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
        this.riskScore = riskScore != null ? Math.max(0, Math.min(100, riskScore)) : 50;
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
