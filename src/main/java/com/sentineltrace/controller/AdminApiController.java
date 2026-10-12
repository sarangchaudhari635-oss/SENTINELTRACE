package com.sentineltrace.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    @GetMapping("/system/status")
    public ResponseEntity<Map<String, Object>> getSystemManagementStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "SECURE",
                "engine", "SentinelTrace SIEM Core",
                "rulesEngine", "ACTIVE",
                "persistence", "PostgreSQL"
        ));
    }
}
