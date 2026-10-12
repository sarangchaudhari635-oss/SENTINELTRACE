package com.sentineltrace.controller;

import com.sentineltrace.dto.DetectionRuleResponse;
import com.sentineltrace.repository.DetectionRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RuleApiController {

    private final DetectionRuleRepository ruleRepository;

    public RuleApiController(DetectionRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public ResponseEntity<List<DetectionRuleResponse>> getRules() {
        List<DetectionRuleResponse> rules = ruleRepository.findAll().stream()
                .map(DetectionRuleResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(rules);
    }
}
