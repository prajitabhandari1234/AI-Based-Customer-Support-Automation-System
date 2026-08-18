package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import com.cqu.coit13230.AIBasedCustomerSupport.knowledge.KnowledgeMatch;

import java.util.List;
import java.util.Optional;

public record OpenAiSupportRequest(
        String customerMessage,
        List<ConversationTurn> conversationHistory,
        Optional<KnowledgeMatch> knowledgeMatch,
        boolean manualTicketRequested
) {
    public OpenAiSupportRequest {
        conversationHistory = conversationHistory == null
                ? List.of()
                : List.copyOf(conversationHistory);
        knowledgeMatch = knowledgeMatch == null
                ? Optional.empty()
                : knowledgeMatch;
    }
}
