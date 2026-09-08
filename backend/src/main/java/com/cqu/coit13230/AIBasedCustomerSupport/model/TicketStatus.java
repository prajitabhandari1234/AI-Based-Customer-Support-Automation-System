package com.cqu.coit13230.AIBasedCustomerSupport.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Lists the possible ticket states.
 */
public enum TicketStatus {
    OPEN,
    ESCALATED,
    IN_PROGRESS,
    ON_HOLD,
    RESOLVED_BY_AI,
    RESOLVED,
    CLOSED;

    // Accepts status labels in a case-insensitive API format.
    @JsonCreator
    public static TicketStatus fromValue(String value) {
        if (value == null) {
            return null;
        }

        if ("NEW".equalsIgnoreCase(value.trim())) {
            return OPEN;
        }

        return TicketStatus.valueOf(value.trim().toUpperCase());
    }
}
