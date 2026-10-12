package com.sentineltrace.repository;

import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetectionRuleRepository extends JpaRepository<DetectionRule, Long> {
    Optional<DetectionRule> findByRuleCode(String ruleCode);
    List<DetectionRule> findByEnabledTrue();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM DetectionRule r WHERE r.eventType = :eventType AND r.enabled = true")
    List<DetectionRule> findByEventTypeAndEnabledTrue(@org.springframework.data.repository.query.Param("eventType") EventType eventType);
}
