package com.sentineltrace.dto;

import com.sentineltrace.entity.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public class IncidentStatusUpdateRequest {

    @NotNull(message = "status is required")
    private IncidentStatus status;

    public IncidentStatusUpdateRequest() {
    }

    public IncidentStatusUpdateRequest(IncidentStatus status) {
        this.status = status;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }
}
