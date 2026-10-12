package com.sentineltrace.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BatchEventIngestRequest {

    @NotEmpty(message = "Events list cannot be empty")
    @Size(max = 100, message = "Batch size cannot exceed 100 events")
    private List<@Valid EventIngestRequest> events;

    public BatchEventIngestRequest() {
    }

    public BatchEventIngestRequest(List<EventIngestRequest> events) {
        this.events = events;
    }

    public List<EventIngestRequest> getEvents() {
        return events;
    }

    public void setEvents(List<EventIngestRequest> events) {
        this.events = events;
    }
}
