package com.sentineltrace.controller;

import com.sentineltrace.dto.DashboardSummaryResponse;
import com.sentineltrace.dto.IncidentDetailResponse;
import com.sentineltrace.dto.SimulationRequest;
import com.sentineltrace.entity.DetectionRule;
import com.sentineltrace.entity.enums.EventType;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import com.sentineltrace.repository.DetectionRuleRepository;
import com.sentineltrace.service.AnalyticsService;
import com.sentineltrace.service.EventIngestionService;
import com.sentineltrace.service.IncidentService;
import com.sentineltrace.simulator.DemoEventSimulator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardWebController {

    private final AnalyticsService analyticsService;
    private final EventIngestionService ingestionService;
    private final IncidentService incidentService;
    private final DetectionRuleRepository ruleRepository;
    private final DemoEventSimulator simulator;

    public DashboardWebController(AnalyticsService analyticsService,
                                  EventIngestionService ingestionService,
                                  IncidentService incidentService,
                                  DetectionRuleRepository ruleRepository,
                                  DemoEventSimulator simulator) {
        this.analyticsService = analyticsService;
        this.ingestionService = ingestionService;
        this.incidentService = incidentService;
        this.ruleRepository = ruleRepository;
        this.simulator = simulator;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardSummaryResponse summary = analyticsService.getDashboardSummary();
        model.addAttribute("summary", summary);
        model.addAttribute("recentEvents", ingestionService.getRecentEvents(10));
        model.addAttribute("rules", ruleRepository.findAll());
        return "dashboard";
    }

    @GetMapping("/events")
    public String events(
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) String sourceIp,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        int boundedSize = Math.min(Math.max(1, size), 50);
        int boundedPage = Math.max(0, page);
        var pageResult = ingestionService.listEvents(
                eventType,
                sourceIp,
                null,
                null,
                PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "occurredAt"))
        );

        model.addAttribute("page", pageResult);
        model.addAttribute("eventType", eventType);
        model.addAttribute("sourceIp", sourceIp);
        model.addAttribute("eventTypes", EventType.values());
        return "events";
    }

    @GetMapping("/incidents")
    public String incidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        int boundedSize = Math.min(Math.max(1, size), 50);
        int boundedPage = Math.max(0, page);
        var pageResult = incidentService.listIncidents(
                status,
                severity,
                PageRequest.of(boundedPage, boundedSize, Sort.by(Sort.Direction.DESC, "lastSeen"))
        );

        model.addAttribute("page", pageResult);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSeverity", severity);
        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("severities", Severity.values());
        return "incidents";
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetail(@PathVariable Long id, Model model) {
        IncidentDetailResponse detail = incidentService.getIncidentById(id);
        model.addAttribute("detail", detail);
        return "incident-detail";
    }

    @PostMapping("/incidents/{id}/status")
    public String updateIncidentStatus(
            @PathVariable Long id,
            @RequestParam IncidentStatus status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            incidentService.updateIncidentStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Incident #" + id + " transitioned to " + status);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/incidents/" + id;
    }

    @GetMapping("/rules")
    public String rules(Model model) {
        List<DetectionRule> rules = ruleRepository.findAll();
        model.addAttribute("rules", rules);
        return "rules";
    }

    @PostMapping("/demo/simulate")
    public String runSimulation(
            @RequestParam String scenario,
            @RequestParam(required = false) Integer count,
            @RequestParam(required = false) String sourceIp,
            RedirectAttributes redirectAttributes
    ) {
        try {
            SimulationRequest req = new SimulationRequest(scenario, count, sourceIp, null);
            Map<String, Object> res = simulator.runSimulation(req);
            redirectAttributes.addFlashAttribute("successMessage", res.get("message"));
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard";
    }
}
