package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;

import java.time.Instant;

public record KnowledgeBaseView(
        Long id,
        String questionPattern,
        String answerTemplate,
        TicketCategory category,
        boolean active,
        String lastUpdatedBy,
        Instant lastUpdatedAt
) {
    public static KnowledgeBaseView from(KnowledgeBaseEntry entry) {
        return new KnowledgeBaseView(
                entry.getId(),
                entry.getQuestionPattern(),
                entry.getAnswerTemplate(),
                entry.getCategory(),
                entry.isActive(),
                entry.getLastUpdatedBy() == null ? null : entry.getLastUpdatedBy().getName(),
                entry.getLastUpdatedAt()
        );
    }
}
