package com.sentineltrace;

import com.sentineltrace.dto.DashboardSummaryResponse;
import com.sentineltrace.dto.EventIngestRequest;
import com.sentineltrace.dto.IncidentResponse;
import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.IncidentAuditLog;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.repository.DetectionRuleRepository;
import com.sentineltrace.repository.IncidentAuditLogRepository;
import com.sentineltrace.repository.IncidentRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import com.sentineltrace.service.AnalyticsService;
import com.sentineltrace.service.EventIngestionService;
import com.sentineltrace.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SentinelTraceIntegrationTest {

    @Autowired
    private EventIngestionService ingestionService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private SecurityEventRepository eventRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private IncidentAuditLogRepository auditLogRepository;

    @Autowired
    private com.sentineltrace.repository.DetectionRuleRepository ruleRepository;

    @BeforeEach
    void cleanState() {
        auditLogRepository.deleteAll();
        incidentRepository.deleteAll();
        eventRepository.deleteAll();

        if (ruleRepository.findByRuleCode("RULE_FAILED_LOGINS").isEmpty()) {
            ruleRepository.save(new DetectionRule(
                    "RULE_FAILED_LOGINS",
                    "Repeated Failed Logins",
                    EventType.LOGIN_FAILURE,
                    5,
                    300,
                    Severity.HIGH,
                    true
            ));
        }
        if (ruleRepository.findByRuleCode("RULE_HTTP_BURST").isEmpty()) {
            ruleRepository.save(new DetectionRule(
                    "RULE_HTTP_BURST",
                    "HTTP Request Burst",
                    EventType.HTTP_REQUEST,
                    30,
                    60,
                    Severity.MEDIUM,
                    true
            ));
        }
    }

    @Test
    @DisplayName("End-to-end: Ingestion, Detection, Incident Deduplication, Status Transition, and Dashboard Metrics")
    void completeEndToEndWorkflowVerification() {
        String testIp = "198.51.100.99";
        Instant baseTime = Instant.now().minusSeconds(60);

        // 1. Ingest 4 failed logins: below threshold
        for (int i = 1; i <= 4; i++) {
            ingestionService.ingestEvent(new EventIngestRequest(
                    EventType.LOGIN_FAILURE,
                    testIp,
                    "target_user",
                    "/login",
                    "Attempt " + i,
                    baseTime.plusSeconds(i * 5L)
            ));
        }

        assertThat(eventRepository.count()).isEqualTo(4);
        assertThat(incidentRepository.count()).isEqualTo(0);

        // 2. Ingest 5th failed login: hits threshold of 5!
        ingestionService.ingestEvent(new EventIngestRequest(
                EventType.LOGIN_FAILURE,
                testIp,
                "target_user",
                "/login",
                "Attempt 5",
                baseTime.plusSeconds(25)
        ));

        assertThat(eventRepository.count()).isEqualTo(5);
        assertThat(incidentRepository.count()).isEqualTo(1);

        Incident createdIncident = incidentRepository.findAll().get(0);
        assertThat(createdIncident.getSourceIp()).isEqualTo(testIp);
        assertThat(createdIncident.getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(createdIncident.getStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(createdIncident.getEvidenceCount()).isEqualTo(5);
        assertThat(createdIncident.getRiskScore()).isEqualTo(75);

        // Verify audit log created
        List<IncidentAuditLog> audits = auditLogRepository.findByIncidentOrderByCreatedAtAsc(createdIncident);
        assertThat(audits).hasSize(1);
        assertThat(audits.get(0).getAction()).isEqualTo("INCIDENT_CREATED");

        // 3. Ingest 6th matching event: updates existing incident (deduplication)
        ingestionService.ingestEvent(new EventIngestRequest(
                EventType.LOGIN_FAILURE,
                testIp,
                "target_user",
                "/login",
                "Attempt 6",
                baseTime.plusSeconds(30)
        ));

        // Still only 1 incident in repository
        assertThat(incidentRepository.count()).isEqualTo(1);
        Incident updatedIncident = incidentRepository.findById(createdIncident.getId()).orElseThrow();
        assertThat(updatedIncident.getEvidenceCount()).isEqualTo(6);
        assertThat(updatedIncident.getRiskScore()).isEqualTo(79); // 75 + 1*4

        // 4. Incident Management: Acknowledge and Resolve
        incidentService.updateIncidentStatus(updatedIncident.getId(), IncidentStatus.ACKNOWLEDGED);
        Incident ackIncident = incidentRepository.findById(updatedIncident.getId()).orElseThrow();
        assertThat(ackIncident.getStatus()).isEqualTo(IncidentStatus.ACKNOWLEDGED);

        incidentService.updateIncidentStatus(updatedIncident.getId(), IncidentStatus.RESOLVED);
        Incident resIncident = incidentRepository.findById(updatedIncident.getId()).orElseThrow();
        assertThat(resIncident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
        assertThat(resIncident.getResolvedAt()).isNotNull();

        // Verify audit log has 3 entries: CREATED -> ACKNOWLEDGED -> RESOLVED
        List<IncidentAuditLog> fullAudits = auditLogRepository.findByIncidentOrderByCreatedAtAsc(resIncident);
        assertThat(fullAudits).hasSize(3);

        // 5. Requirement 11: Dashboard metrics reflect persisted data
        DashboardSummaryResponse summary = analyticsService.getDashboardSummary();
        assertThat(summary.getTotalEvents()).isEqualTo(6);
        assertThat(summary.getFailedLoginCount()).isEqualTo(6);
        assertThat(summary.getOpenIncidents()).isEqualTo(0);
        assertThat(summary.getResolvedIncidents()).isEqualTo(1);
        assertThat(summary.getSeverityDistribution().get("HIGH")).isEqualTo(1);
    }
}
