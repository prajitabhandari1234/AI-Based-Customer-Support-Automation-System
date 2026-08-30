package com.cqu.coit13230.AIBasedCustomerSupport.chat;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketSummary;

public record ChatResponse(
        String reply,
        TicketSummary ticket,
        AiAnalysisView analysis
) {}
