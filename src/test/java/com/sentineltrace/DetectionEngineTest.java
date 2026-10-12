package com.sentineltrace;

import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.repository.DetectionRuleRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import com.sentineltrace.service.DetectionEngine;
import com.sentineltrace.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectionEngineTest {

    @Mock
    private DetectionRuleRepository ruleRepository;

    @Mock
    private SecurityEventRepository eventRepository;

    @Mock
    private IncidentService incidentService;

    private DetectionEngine detectionEngine;
    private DetectionRule failedLoginRule;

    @BeforeEach
    void setUp() {
        detectionEngine = new DetectionEngine(ruleRepository, eventRepository, incidentService);

        failedLoginRule = new DetectionRule(
                "RULE_FAILED_LOGINS",
                "Repeated Failed Logins",
                EventType.LOGIN_FAILURE,
                5,
                300, // 5 minutes (300 seconds)
                Severity.HIGH,
                true
        );
        failedLoginRule.setId(1L);
    }

    @Test
    @DisplayName("Requirement 1: Four failed logins do not trigger a five-event threshold")
    void fourFailedLoginsDoNotTriggerFiveEventThreshold() {
        when(ruleRepository.findByEventTypeAndEnabledTrue(EventType.LOGIN_FAILURE))
                .thenReturn(List.of(failedLoginRule));

        Instant now = Instant.now();
        SecurityEvent event = new SecurityEvent(EventType.LOGIN_FAILURE, "192.168.1.50", "admin", "/login", "Fail 4", now);

        // Count is 4 (below threshold of 5)
        when(eventRepository.countByEventTypeAndSourceIpAndOccurredAtBetween(
                eq(EventType.LOGIN_FAILURE), eq("192.168.1.50"), any(Instant.class), any(Instant.class)))
                .thenReturn(4L);

        List<Incident> matched = detectionEngine.evaluateEvent(event);

        assertThat(matched).isEmpty();
        verify(incidentService, never()).createOrUpdateIncident(any(), anyString(), anyString(), anyString(), any(), anyInt(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Requirement 2: The fifth qualifying failed login triggers an incident")
    void fifthQualifyingFailedLoginTriggersIncident() {
        when(ruleRepository.findByEventTypeAndEnabledTrue(EventType.LOGIN_FAILURE))
                .thenReturn(List.of(failedLoginRule));

        Instant now = Instant.now();
        SecurityEvent event = new SecurityEvent(EventType.LOGIN_FAILURE, "192.168.1.50", "admin", "/login", "Fail 5", now);

        // Count is 5 (meets threshold)
        when(eventRepository.countByEventTypeAndSourceIpAndOccurredAtBetween(
                eq(EventType.LOGIN_FAILURE), eq("192.168.1.50"), any(Instant.class), any(Instant.class)))
                .thenReturn(5L);

        Incident mockIncident = new Incident(failedLoginRule, "RULE_FAILED_LOGINS:192.168.1.50", "192.168.1.50",
                "Title", Severity.HIGH, 75, null, now.minusSeconds(300), now, 5);
        mockIncident.setId(10L);

        when(incidentService.createOrUpdateIncident(
                eq(failedLoginRule),
                eq("RULE_FAILED_LOGINS:192.168.1.50"),
                eq("192.168.1.50"),
                anyString(),
                eq(Severity.HIGH),
                anyInt(),
                any(Instant.class),
                eq(now),
                eq(5)))
                .thenReturn(mockIncident);

        List<Incident> matched = detectionEngine.evaluateEvent(event);

        assertThat(matched).hasSize(1);
        assertThat(matched.get(0).getId()).isEqualTo(10L);
        verify(incidentService, times(1)).createOrUpdateIncident(any(), anyString(), anyString(), anyString(), any(), anyInt(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Requirement 3: Events outside the time window do not count")
    void eventsOutsideTimeWindowDoNotCount() {
        when(ruleRepository.findByEventTypeAndEnabledTrue(EventType.LOGIN_FAILURE))
                .thenReturn(List.of(failedLoginRule));

        Instant now = Instant.now();
        SecurityEvent event = new SecurityEvent(EventType.LOGIN_FAILURE, "192.168.1.50", "admin", "/login", "New event", now);

        // Even if there were 10 historical events, within window there are only 3
        when(eventRepository.countByEventTypeAndSourceIpAndOccurredAtBetween(
                eq(EventType.LOGIN_FAILURE), eq("192.168.1.50"), any(Instant.class), any(Instant.class)))
                .thenReturn(3L);

        List<Incident> matched = detectionEngine.evaluateEvent(event);

        assertThat(matched).isEmpty();
        verify(incidentService, never()).createOrUpdateIncident(any(), anyString(), anyString(), anyString(), any(), anyInt(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Requirement 4: Different source IPs are counted independently")
    void differentSourceIPsAreCountedIndependently() {
        when(ruleRepository.findByEventTypeAndEnabledTrue(EventType.LOGIN_FAILURE))
                .thenReturn(List.of(failedLoginRule));

        Instant now = Instant.now();
        // Event for IP 10.0.0.1
        SecurityEvent eventIp1 = new SecurityEvent(EventType.LOGIN_FAILURE, "10.0.0.1", "user1", "/login", "Fail", now);
        // Event for IP 10.0.0.2
        SecurityEvent eventIp2 = new SecurityEvent(EventType.LOGIN_FAILURE, "10.0.0.2", "user2", "/login", "Fail", now);

        // IP 1 has 4 events in window
        when(eventRepository.countByEventTypeAndSourceIpAndOccurredAtBetween(
                eq(EventType.LOGIN_FAILURE), eq("10.0.0.1"), any(Instant.class), any(Instant.class)))
                .thenReturn(4L);

        // IP 2 has 3 events in window
        when(eventRepository.countByEventTypeAndSourceIpAndOccurredAtBetween(
                eq(EventType.LOGIN_FAILURE), eq("10.0.0.2"), any(Instant.class), any(Instant.class)))
                .thenReturn(3L);

        List<Incident> matched1 = detectionEngine.evaluateEvent(eventIp1);
        List<Incident> matched2 = detectionEngine.evaluateEvent(eventIp2);

        // Neither triggers because counts are isolated per source IP
        assertThat(matched1).isEmpty();
        assertThat(matched2).isEmpty();
        verify(incidentService, never()).createOrUpdateIncident(any(), anyString(), anyString(), anyString(), any(), anyInt(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Requirement 5: A disabled rule does not generate incidents")
    void disabledRuleDoesNotGenerateIncidents() {
        // Disabled rule is not returned by ruleRepository.findByEventTypeAndEnabledTrue
        when(ruleRepository.findByEventTypeAndEnabledTrue(EventType.LOGIN_FAILURE))
                .thenReturn(List.of());

        Instant now = Instant.now();
        SecurityEvent event = new SecurityEvent(EventType.LOGIN_FAILURE, "192.168.1.50", "admin", "/login", "Attempt", now);

        List<Incident> matched = detectionEngine.evaluateEvent(event);

        assertThat(matched).isEmpty();
        verifyNoInteractions(eventRepository);
        verify(incidentService, never()).createOrUpdateIncident(any(), anyString(), anyString(), anyString(), any(), anyInt(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("Requirement 6 (Scoring): Transparent, bounded risk scoring remains between 0 and 100")
    void riskScoringIsTransparentAndBounded() {
        // At exact threshold (5 events): base HIGH score = 75
        int scoreAtThreshold = detectionEngine.calculateRiskScore(failedLoginRule, 5);
        assertThat(scoreAtThreshold).isEqualTo(75);

        // With 7 events (excess 2): 75 + 2*4 = 83
        int scoreWithExcess = detectionEngine.calculateRiskScore(failedLoginRule, 7);
        assertThat(scoreWithExcess).isEqualTo(83);

        // With extreme excess (500 events): capped at 100
        int scoreMax = detectionEngine.calculateRiskScore(failedLoginRule, 500);
        assertThat(scoreMax).isEqualTo(100);
        assertThat(scoreMax).isBetween(0, 100);
    }
}
