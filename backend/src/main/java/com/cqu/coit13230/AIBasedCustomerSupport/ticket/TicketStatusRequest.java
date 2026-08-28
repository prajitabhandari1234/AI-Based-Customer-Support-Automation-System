package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import jakarta.validation.constraints.NotNull;

public record TicketStatusRequest(
        @NotNull TicketStatus status,
        String resolutionNotes
) {}
