package com.sentineltrace.service;

import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.repository.DetectionRuleRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DetectionEngine {

    private static final Logger log = LoggerFactory.getLogger(DetectionEngine.class);

    private final DetectionRuleRepository ruleRepository;
    private final SecurityEventRepository eventRepository;
    private final IncidentService incidentService;

    public DetectionEngine(DetectionRuleRepository ruleRepository,
                           SecurityEventRepository eventRepository,
                           IncidentService incidentService) {
        this.ruleRepository = ruleRepository;
        this.eventRepository = eventRepository;
        this.incidentService = incidentService;
    }

    /**
     * Evaluates all enabled detection rules matching the ingested event's type.
     * Uses rolling time windows and deterministic threshold counts.
     */
    @Transactional
    public List<Incident> evaluateEvent(SecurityEvent event) {
        List<Incident> matchedIncidents = new ArrayList<>();
        List<DetectionRule> matchingRules = ruleRepository.findByEventTypeAndEnabledTrue(event.getEventType());

        if (matchingRules.isEmpty()) {
            return matchedIncidents;
        }

        Instant eventTime = event.getOccurredAt();
        String sourceIp = event.getSourceIp();

        for (DetectionRule rule : matchingRules) {
            Instant windowStart = eventTime.minusSeconds(rule.getWindowSeconds());
            Instant windowEnd = eventTime.plusSeconds(1);

            long evidenceCount = eventRepository.countByEventTypeAndSourceIpAndOccurredAtBetween(
                    rule.getEventType(),
                    sourceIp,
                    windowStart,
                    windowEnd
            );

            log.info("Rule [{}] evaluation for IP {}: {} events in window [{} to {}] (threshold: {})",
                    rule.getRuleCode(), sourceIp, evidenceCount, windowStart, windowEnd, rule.getThreshold());

            if (evidenceCount >= rule.getThreshold()) {
                String fingerprint = buildFingerprint(rule.getRuleCode(), sourceIp);
                String title = buildIncidentTitle(rule, sourceIp, evidenceCount);
                int riskScore = calculateRiskScore(rule, evidenceCount);

                Incident incident = incidentService.createOrUpdateIncident(
                        rule,
                        fingerprint,
                        sourceIp,
                        title,
                        rule.getSeverity(),
                        riskScore,
                        windowStart,
                        eventTime,
                        (int) evidenceCount
                );
                matchedIncidents.add(incident);
                log.info("Detection rule [{}] matched for source IP {} (evidence count: {}). Incident ID: {}",
                        rule.getRuleCode(), sourceIp, evidenceCount, incident.getId());
            }
        }

        return matchedIncidents;
    }

    public String buildFingerprint(String ruleCode, String sourceIp) {
        return ruleCode.trim() + ":" + sourceIp.trim().toLowerCase();
    }

    private String buildIncidentTitle(DetectionRule rule, String sourceIp, long count) {
        return String.format("%s: %d events detected from %s within %ds window",
                rule.getName(), count, sourceIp, rule.getWindowSeconds());
    }

    /**
     * Transparent, bounded risk scoring between 0 and 100.
     * Starts with a baseline based on severity, plus incremental points per evidence beyond threshold.
     */
    public int calculateRiskScore(DetectionRule rule, long evidenceCount) {
        int baseScore = switch (rule.getSeverity()) {
            case LOW -> 30;
            case MEDIUM -> 50;
            case HIGH -> 75;
            case CRITICAL -> 90;
        };

        long excess = Math.max(0, evidenceCount - rule.getThreshold());
        int bonus = (int) Math.min(100 - baseScore, excess * 4);
        int finalScore = baseScore + bonus;
        return Math.max(0, Math.min(100, finalScore));
    }
}
