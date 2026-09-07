package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import com.cqu.coit13230.AIBasedCustomerSupport.message.MessageView;

import java.time.Instant;
import java.util.List;

public record TicketDetail(
        TicketSummary ticket,
        String escalationReason,
        String resolutionNotes,
        Instant firstResponseAt,
        Instant resolvedAt,
        Instant closedAt,
        List<MessageView> messages
) {}
