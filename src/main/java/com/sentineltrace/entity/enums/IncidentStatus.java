package com.sentineltrace.entity.enums;

public enum IncidentStatus {
    OPEN,
    ACKNOWLEDGED,
    RESOLVED;

    public boolean canTransitionTo(IncidentStatus target) {
        if (target == null || target == this) {
            return false;
        }
        return switch (this) {
            case OPEN -> target == ACKNOWLEDGED || target == RESOLVED;
            case ACKNOWLEDGED -> target == RESOLVED;
            case RESOLVED -> false;
        };
    }
}
