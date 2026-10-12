package com.sentineltrace.dto;

import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.Severity;

public class DetectionRuleResponse {

    private Long id;
    private String ruleCode;
    private String name;
    private EventType eventType;
    private Integer threshold;
    private Integer windowSeconds;
    private Severity severity;
    private Boolean enabled;

    public DetectionRuleResponse() {
    }

    public static DetectionRuleResponse fromEntity(DetectionRule rule) {
        if (rule == null) return null;
        DetectionRuleResponse dto = new DetectionRuleResponse();
        dto.setId(rule.getId());
        dto.setRuleCode(rule.getRuleCode());
        dto.setName(rule.getName());
        dto.setEventType(rule.getEventType());
        dto.setThreshold(rule.getThreshold());
        dto.setWindowSeconds(rule.getWindowSeconds());
        dto.setSeverity(rule.getSeverity());
        dto.setEnabled(rule.getEnabled());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(Integer windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
