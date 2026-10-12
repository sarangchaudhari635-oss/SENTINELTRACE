package com.sentineltrace;

import com.sentineltrace.dto.IncidentResponse;
import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.IncidentAuditLog;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.exception.InvalidStateTransitionException;
import com.sentineltrace.exception.ResourceNotFoundException;
import com.sentineltrace.repository.IncidentAuditLogRepository;
import com.sentineltrace.repository.IncidentRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import com.sentineltrace.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentAuditLogRepository auditLogRepository;

    @Mock
    private SecurityEventRepository eventRepository;

    private IncidentService incidentService;
    private DetectionRule rule;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(incidentRepository, auditLogRepository, eventRepository);
        rule = new DetectionRule("RULE_FAILED_LOGINS", "Failed Logins", EventType.LOGIN_FAILURE, 5, 300, Severity.HIGH, true);
        rule.setId(1L);
    }

    @Test
    @DisplayName("Requirement 6: Repeated matching events update an active incident instead of creating duplicates")
    void repeatedMatchingEventsUpdateActiveIncident() {
        String fingerprint = "RULE_FAILED_LOGINS:192.168.1.10";
        Instant initialTime = Instant.now().minusSeconds(100);
        Incident existingIncident = new Incident(rule, fingerprint, "192.168.1.10", "Initial Title",
                Severity.HIGH, 75, IncidentStatus.OPEN, initialTime, initialTime, 5);
        existingIncident.setId(42L);

        when(incidentRepository.findByFingerprintAndStatusIn(eq(fingerprint), anyCollection()))
                .thenReturn(Optional.of(existingIncident));
        when(incidentRepository.saveAndFlush(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant newTime = Instant.now();
        Incident result = incidentService.createOrUpdateIncident(
                rule, fingerprint, "192.168.1.10", "Updated Title", Severity.HIGH, 83, initialTime, newTime, 7
        );

        assertThat(result.getId()).isEqualTo(42L);
        assertThat(result.getEvidenceCount()).isEqualTo(7);
        assertThat(result.getLastSeen()).isEqualTo(newTime);
        assertThat(result.getRiskScore()).isEqualTo(83);

        // Crucial: No duplicate incident created, only save existing incident
        verify(incidentRepository, times(1)).saveAndFlush(existingIncident);
        // Initial audit log for creation should NOT be called again
        verify(auditLogRepository, never()).saveAndFlush(any(IncidentAuditLog.class));
    }

    @Test
    @DisplayName("Requirement 8: Unknown incident ID throws ResourceNotFoundException (404)")
    void unknownIncidentIdThrowsNotFound() {
        when(incidentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.getIncidentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Incident not found with ID: 999");
    }

    @Test
    @DisplayName("Requirement 9: Invalid incident state transitions are rejected (409)")
    void invalidIncidentStateTransitionsAreRejected() {
        Incident resolvedIncident = new Incident(rule, "fp", "1.1.1.1", "Resolved Inc",
                Severity.HIGH, 75, IncidentStatus.RESOLVED, Instant.now(), Instant.now(), 5);
        resolvedIncident.setId(15L);

        when(incidentRepository.findById(15L)).thenReturn(Optional.of(resolvedIncident));

        // Attempting to move from RESOLVED back to OPEN
        assertThatThrownBy(() -> incidentService.updateIncidentStatus(15L, IncidentStatus.OPEN))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Invalid status transition: cannot transition incident 15 from RESOLVED to OPEN");

        // Attempting to move from RESOLVED to ACKNOWLEDGED
        assertThatThrownBy(() -> incidentService.updateIncidentStatus(15L, IncidentStatus.ACKNOWLEDGED))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Requirement 10: Incident status changes create audit entries")
    void incidentStatusChangesCreateAuditEntries() {
        Incident openIncident = new Incident(rule, "fp", "1.1.1.1", "Open Inc",
                Severity.HIGH, 75, IncidentStatus.OPEN, Instant.now(), Instant.now(), 5);
        openIncident.setId(25L);

        when(incidentRepository.findById(25L)).thenReturn(Optional.of(openIncident));
        when(incidentRepository.saveAndFlush(any(Incident.class))).thenAnswer(inv -> inv.getArgument(0));

        // Acknowledge incident
        IncidentResponse response = incidentService.updateIncidentStatus(25L, IncidentStatus.ACKNOWLEDGED);

        assertThat(response.getStatus()).isEqualTo(IncidentStatus.ACKNOWLEDGED);

        // Verify audit log creation
        ArgumentCaptor<IncidentAuditLog> captor = ArgumentCaptor.forClass(IncidentAuditLog.class);
        verify(auditLogRepository, times(1)).saveAndFlush(captor.capture());

        IncidentAuditLog savedLog = captor.getValue();
        assertThat(savedLog.getIncident().getId()).isEqualTo(25L);
        assertThat(savedLog.getAction()).isEqualTo("STATUS_ACKNOWLEDGED");
        assertThat(savedLog.getPreviousStatus()).isEqualTo(IncidentStatus.OPEN);
        assertThat(savedLog.getNewStatus()).isEqualTo(IncidentStatus.ACKNOWLEDGED);
    }
}
