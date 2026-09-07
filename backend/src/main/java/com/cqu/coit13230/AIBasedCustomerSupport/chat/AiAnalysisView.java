package com.cqu.coit13230.AIBasedCustomerSupport.chat;

import com.cqu.coit13230.AIBasedCustomerSupport.ai.AiResult;
import com.cqu.coit13230.AIBasedCustomerSupport.ai.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketPriority;

import java.util.List;

public record AiAnalysisView(
        TicketCategory category,
        TicketPriority priority,
        Sentiment sentiment,
        double sentimentScore,
        double confidence,
        boolean escalated,
        List<String> escalationReasons,
        boolean knowledgeBaseMatch
) {
    public static AiAnalysisView from(AiResult result) {
        return new AiAnalysisView(
                result.category(), result.priority(), result.sentiment(), result.sentimentScore(),
                result.confidence(), result.escalated(), result.escalationReasons(), result.knowledgeBaseMatch()
        );
    }
}
