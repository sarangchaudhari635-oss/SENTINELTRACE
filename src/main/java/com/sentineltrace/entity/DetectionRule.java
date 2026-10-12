package com.sentineltrace.entity;

import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.Severity;
import jakarta.persistence.*;

@Entity
@Table(name = "detection_rules")
public class DetectionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 60)
    private String ruleCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType;

    @Column(nullable = false)
    private Integer threshold;

    @Column(name = "window_seconds", nullable = false)
    private Integer windowSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false)
    private Boolean enabled = true;

    public DetectionRule() {
    }

    public DetectionRule(String ruleCode, String name, EventType eventType, Integer threshold, Integer windowSeconds, Severity severity, Boolean enabled) {
        this.ruleCode = ruleCode;
        this.name = name;
        this.eventType = eventType;
        this.threshold = threshold;
        this.windowSeconds = windowSeconds;
        this.severity = severity;
        this.enabled = enabled;
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
