package com.sentineltrace.service;

import com.sentineltrace.dto.*;
import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.IncidentAuditLog;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.exception.InvalidStateTransitionException;
import com.sentineltrace.exception.ResourceNotFoundException;
import com.sentineltrace.repository.IncidentAuditLogRepository;
import com.sentineltrace.repository.IncidentRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    private final IncidentRepository incidentRepository;
    private final IncidentAuditLogRepository auditLogRepository;
    private final SecurityEventRepository eventRepository;

    public IncidentService(IncidentRepository incidentRepository,
                           IncidentAuditLogRepository auditLogRepository,
                           SecurityEventRepository eventRepository) {
        this.incidentRepository = incidentRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Incident createOrUpdateIncident(DetectionRule rule,
                                          String fingerprint,
                                          String sourceIp,
                                          String title,
                                          Severity severity,
                                          int riskScore,
                                          Instant windowStart,
                                          Instant windowEnd,
                                          int evidenceCount) {
        // Look for an existing active incident with matching fingerprint
        Optional<Incident> existingOpt = incidentRepository.findByFingerprintAndStatusIn(
                fingerprint,
                List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
        );

        if (existingOpt.isPresent()) {
            Incident incident = existingOpt.get();
            incident.setLastSeen(windowEnd);
            incident.setEvidenceCount(evidenceCount);
            incident.setRiskScore(Math.max(incident.getRiskScore(), riskScore));
            incident.setTitle(title);

            Incident saved = incidentRepository.saveAndFlush(incident);
            log.info("Updated existing incident [ID: {}] with evidence count: {}", saved.getId(), evidenceCount);
            return saved;
        }

        // Create a new incident
        Incident newIncident = new Incident(
                rule,
                fingerprint,
                sourceIp,
                title,
                severity,
                riskScore,
                IncidentStatus.OPEN,
                windowStart,
                windowEnd,
                evidenceCount
        );
        Incident savedIncident = incidentRepository.saveAndFlush(newIncident);

        // Record initial audit entry
        IncidentAuditLog auditLog = new IncidentAuditLog(
                savedIncident,
                "INCIDENT_CREATED",
                null,
                IncidentStatus.OPEN
        );
        auditLogRepository.saveAndFlush(auditLog);

        log.info("Created new incident [ID: {}] for fingerprint: {}", savedIncident.getId(), fingerprint);
        return savedIncident;
    }

    @Transactional(readOnly = true)
    public Page<IncidentResponse> listIncidents(IncidentStatus status, Severity severity, Pageable pageable) {
        return incidentRepository.findWithFilters(status, severity, pageable)
                .map(IncidentResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public IncidentDetailResponse getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        DetectionRule rule = incident.getRule();
        DetectionRuleResponse ruleResponse = DetectionRuleResponse.fromEntity(rule);

        // Supporting evidence events in the detection window
        Instant startWindow = incident.getFirstSeen();
        Instant endWindow = incident.getLastSeen();
        if (rule != null && startWindow != null) {
            // Include margin around the incident window to present full context
            Instant expandedStart = startWindow.minusSeconds(30);
            Instant expandedEnd = endWindow.plusSeconds(30);
            List<SecurityEvent> events = eventRepository.findByEventTypeAndSourceIpAndOccurredAtBetweenOrderByOccurredAtAsc(
                    rule.getEventType(),
                    incident.getSourceIp(),
                    expandedStart,
                    expandedEnd
            );
            List<EventResponse> eventResponses = events.stream()
                    .map(EventResponse::fromEntity)
                    .toList();

            List<AuditLogResponse> auditLogs = auditLogRepository.findByIncidentOrderByCreatedAtAsc(incident)
                    .stream()
                    .map(AuditLogResponse::fromEntity)
                    .toList();

            return new IncidentDetailResponse(
                    IncidentResponse.fromEntity(incident),
                    ruleResponse,
                    eventResponses,
                    auditLogs
            );
        }

        List<AuditLogResponse> auditLogs = auditLogRepository.findByIncidentOrderByCreatedAtAsc(incident)
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();

        return new IncidentDetailResponse(
                IncidentResponse.fromEntity(incident),
                ruleResponse,
                List.of(),
                auditLogs
        );
    }

    @Transactional
    public IncidentResponse updateIncidentStatus(Long id, IncidentStatus targetStatus) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with ID: " + id));

        IncidentStatus currentStatus = incident.getStatus();

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Invalid status transition: cannot transition incident %d from %s to %s",
                            id, currentStatus, targetStatus)
            );
        }

        incident.setStatus(targetStatus);
        if (targetStatus == IncidentStatus.RESOLVED) {
            incident.setResolvedAt(Instant.now());
        }

        Incident saved = incidentRepository.saveAndFlush(incident);

        // Create audit log entry
        IncidentAuditLog auditLog = new IncidentAuditLog(
                saved,
                targetStatus == IncidentStatus.ACKNOWLEDGED ? "STATUS_ACKNOWLEDGED" : "STATUS_RESOLVED",
                currentStatus,
                targetStatus
        );
        auditLogRepository.saveAndFlush(auditLog);

        log.info("Transitioned incident [ID: {}] from {} to {}", id, currentStatus, targetStatus);
        return IncidentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getRecentIncidents(int limit) {
        return incidentRepository.findTop10ByOrderByLastSeenDesc()
                .stream()
                .limit(limit)
                .map(IncidentResponse::fromEntity)
                .toList();
    }
}
