package com.sentineltrace.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {

    private long totalEvents;
    private long failedLoginCount;
    private long openIncidents;
    private long acknowledgedIncidents;
    private long resolvedIncidents;
    private Map<String, Long> severityDistribution;
    private List<IncidentResponse> recentIncidents;
    private List<TimelinePoint> eventTimeline;

    public DashboardSummaryResponse() {
    }

    public static class TimelinePoint {
        private String timestamp;
        private long totalCount;
        private long failedLoginCount;

        public TimelinePoint() {
        }

        public TimelinePoint(String timestamp, long totalCount, long failedLoginCount) {
            this.timestamp = timestamp;
            this.totalCount = totalCount;
            this.failedLoginCount = failedLoginCount;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }

        public long getFailedLoginCount() {
            return failedLoginCount;
        }

        public void setFailedLoginCount(long failedLoginCount) {
            this.failedLoginCount = failedLoginCount;
        }
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }

    public long getFailedLoginCount() {
        return failedLoginCount;
    }

    public void setFailedLoginCount(long failedLoginCount) {
        this.failedLoginCount = failedLoginCount;
    }

    public long getOpenIncidents() {
        return openIncidents;
    }

    public void setOpenIncidents(long openIncidents) {
        this.openIncidents = openIncidents;
    }

    public long getAcknowledgedIncidents() {
        return acknowledgedIncidents;
    }

    public void setAcknowledgedIncidents(long acknowledgedIncidents) {
        this.acknowledgedIncidents = acknowledgedIncidents;
    }

    public long getResolvedIncidents() {
        return resolvedIncidents;
    }

    public void setResolvedIncidents(long resolvedIncidents) {
        this.resolvedIncidents = resolvedIncidents;
    }

    public Map<String, Long> getSeverityDistribution() {
        return severityDistribution;
    }

    public void setSeverityDistribution(Map<String, Long> severityDistribution) {
        this.severityDistribution = severityDistribution;
    }

    public List<IncidentResponse> getRecentIncidents() {
        return recentIncidents;
    }

    public void setRecentIncidents(List<IncidentResponse> recentIncidents) {
        this.recentIncidents = recentIncidents;
    }

    public List<TimelinePoint> getEventTimeline() {
        return eventTimeline;
    }

    public void setEventTimeline(List<TimelinePoint> eventTimeline) {
        this.eventTimeline = eventTimeline;
    }
}
