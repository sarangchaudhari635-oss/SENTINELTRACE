package com.sentineltrace.controller;

import com.sentineltrace.dto.IncidentDetailResponse;
import com.sentineltrace.dto.IncidentResponse;
import com.sentineltrace.dto.IncidentStatusUpdateRequest;
import com.sentineltrace.dto.PageResponse;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentApiController {

    private final IncidentService incidentService;

    public IncidentApiController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncidentResponse>> listIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int boundedSize = Math.min(Math.max(1, size), 100);
        int boundedPage = Math.max(0, page);
        Pageable pageable = PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "lastSeen"));
        PageResponse<IncidentResponse> response = PageResponse.of(incidentService.listIncidents(status, severity, pageable));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> getIncident(@PathVariable Long id) {
        IncidentDetailResponse detail = incidentService.getIncidentById(id);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<IncidentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody IncidentStatusUpdateRequest request
    ) {
        IncidentResponse updated = incidentService.updateIncidentStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }
}
