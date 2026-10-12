package com.sentineltrace.service;

import com.sentineltrace.dto.DashboardSummaryResponse;
import com.sentineltrace.dto.IncidentResponse;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.repository.IncidentRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AnalyticsService {

    private final SecurityEventRepository eventRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneOffset.UTC);

    public AnalyticsService(SecurityEventRepository eventRepository,
                            IncidentRepository incidentRepository,
                            IncidentService incidentService) {
        this.eventRepository = eventRepository;
        this.incidentRepository = incidentRepository;
        this.incidentService = incidentService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        DashboardSummaryResponse summary = new DashboardSummaryResponse();

        long totalEvents = eventRepository.count();
        long failedLogins = eventRepository.countByEventType(EventType.LOGIN_FAILURE);
        long openIncidents = incidentRepository.countByStatus(IncidentStatus.OPEN);
        long ackIncidents = incidentRepository.countByStatus(IncidentStatus.ACKNOWLEDGED);
        long resolvedIncidents = incidentRepository.countByStatus(IncidentStatus.RESOLVED);

        summary.setTotalEvents(totalEvents);
        summary.setFailedLoginCount(failedLogins);
        summary.setOpenIncidents(openIncidents);
        summary.setAcknowledgedIncidents(ackIncidents);
        summary.setResolvedIncidents(resolvedIncidents);

        // Severity Distribution
        Map<String, Long> severityDist = new LinkedHashMap<>();
        for (Severity severity : Severity.values()) {
            severityDist.put(severity.name(), incidentRepository.countBySeverity(severity));
        }
        summary.setSeverityDistribution(severityDist);

        // Recent Incidents
        List<IncidentResponse> recent = incidentService.getRecentIncidents(10);
        summary.setRecentIncidents(recent);

        // Timeline for charts: group past 1 hour events into 5-minute buckets
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        List<SecurityEvent> recentEvents = eventRepository.findEventsSince(oneHourAgo);

        Map<String, long[]> bucketMap = new LinkedHashMap<>();
        // Initialize timeline buckets
        Instant current = oneHourAgo;
        Instant now = Instant.now();
        while (!current.isAfter(now)) {
            String label = TIME_FORMATTER.format(current);
            bucketMap.put(label, new long[]{0, 0}); // [total, failedLogins]
            current = current.plusSeconds(300); // 5 min intervals
        }

        for (SecurityEvent e : recentEvents) {
            String label = TIME_FORMATTER.format(e.getOccurredAt());
            if (bucketMap.containsKey(label)) {
                bucketMap.get(label)[0]++;
                if (e.getEventType() == EventType.LOGIN_FAILURE) {
                    bucketMap.get(label)[1]++;
                }
            } else {
                long[] counts = bucketMap.computeIfAbsent(label, k -> new long[]{0, 0});
                counts[0]++;
                if (e.getEventType() == EventType.LOGIN_FAILURE) {
                    counts[1]++;
                }
            }
        }

        List<DashboardSummaryResponse.TimelinePoint> timelinePoints = new ArrayList<>();
        bucketMap.forEach((time, counts) -> {
            timelinePoints.add(new DashboardSummaryResponse.TimelinePoint(time, counts[0], counts[1]));
        });
        summary.setEventTimeline(timelinePoints);

        return summary;
    }
}
