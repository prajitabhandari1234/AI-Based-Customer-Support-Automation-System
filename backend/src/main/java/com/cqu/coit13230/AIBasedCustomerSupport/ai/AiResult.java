package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketPriority;

import java.util.List;

public record AiResult(
        String reply,
        TicketCategory category,
        TicketPriority priority,
        Sentiment sentiment,
        double sentimentScore,
        double confidence,
        boolean escalated,
        List<String> escalationReasons,
        boolean knowledgeBaseMatch
) {}
