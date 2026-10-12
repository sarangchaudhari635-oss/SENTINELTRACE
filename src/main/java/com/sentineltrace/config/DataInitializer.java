package com.sentineltrace.config;

import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.repository.DetectionRuleRepository;
import com.sentineltrace.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DetectionRuleRepository ruleRepository;
    private final SecurityEventRepository eventRepository;

    public DataInitializer(DetectionRuleRepository ruleRepository, SecurityEventRepository eventRepository) {
        this.ruleRepository = ruleRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        initDetectionRules();
        initSampleEventsIfEmpty();
    }

    private void initDetectionRules() {
        if (ruleRepository.findByRuleCode("RULE_FAILED_LOGINS").isEmpty()) {
            DetectionRule failedLoginRule = new DetectionRule(
                    "RULE_FAILED_LOGINS",
                    "Repeated Failed Logins",
                    EventType.LOGIN_FAILURE,
                    5,
                    300, // 5 minutes rolling window
                    Severity.HIGH,
                    true
            );
            ruleRepository.save(failedLoginRule);
            log.info("Initialized default detection rule: RULE_FAILED_LOGINS");
        }

        if (ruleRepository.findByRuleCode("RULE_HTTP_BURST").isEmpty()) {
            DetectionRule httpBurstRule = new DetectionRule(
                    "RULE_HTTP_BURST",
                    "HTTP Request Burst",
                    EventType.HTTP_REQUEST,
                    30,
                    60, // 60 seconds rolling window
                    Severity.MEDIUM,
                    true
            );
            ruleRepository.save(httpBurstRule);
            log.info("Initialized default detection rule: RULE_HTTP_BURST");
        }
    }

    private void initSampleEventsIfEmpty() {
        if (eventRepository.count() == 0) {
            log.info("Seeding initial benign sample events for dashboard demonstration...");
            Instant now = Instant.now();
            List<SecurityEvent> seedEvents = List.of(
                    new SecurityEvent(EventType.LOGIN_SUCCESS, "192.168.1.100", "admin", "/login", "Admin session established", now.minusSeconds(1200)),
                    new SecurityEvent(EventType.HTTP_REQUEST, "192.168.1.100", "admin", "/dashboard", "GET /dashboard 200 OK", now.minusSeconds(1100)),
                    new SecurityEvent(EventType.LOGIN_SUCCESS, "192.168.1.105", "sarang", "/login", "User authentication successful", now.minusSeconds(900)),
                    new SecurityEvent(EventType.HTTP_REQUEST, "192.168.1.105", "sarang", "/api/profile", "GET /api/profile 200 OK", now.minusSeconds(850)),
                    new SecurityEvent(EventType.PASSWORD_RESET, "192.168.1.110", "alice", "/auth/reset", "Password reset initiated", now.minusSeconds(600)),
                    new SecurityEvent(EventType.HTTP_REQUEST, "192.168.1.110", "alice", "/home", "GET /home 200 OK", now.minusSeconds(400))
            );
            eventRepository.saveAll(seedEvents);
            log.info("Seeded {} baseline events", seedEvents.size());
        }
    }
}
