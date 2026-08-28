package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import com.cqu.coit13230.AIBasedCustomerSupport.ai.Sentiment;

import java.time.Instant;

public record TicketSummary(
        Long id,
        String title,
        Long customerId,
        String customerName,
        Long assignedAgentId,
        String assignedAgentName,
        TicketCategory category,
        TicketPriority priority,
        TicketStatus status,
        Sentiment sentiment,
        double sentimentScore,
        double aiConfidenceScore,
        boolean escalated,
        Instant createdAt,
        Instant updatedAt
) {
    public static TicketSummary from(Ticket ticket) {
        return new TicketSummary(
                ticket.getId(), ticket.getTitle(),
                ticket.getCustomer().getId(), ticket.getCustomer().getName(),
                ticket.getAssignedAgent() == null ? null : ticket.getAssignedAgent().getId(),
                ticket.getAssignedAgent() == null ? null : ticket.getAssignedAgent().getName(),
                ticket.getCategory(), ticket.getPriority(), ticket.getStatus(),
                ticket.getSentiment(), ticket.getSentimentScore(), ticket.getAiConfidenceScore(),
                ticket.isEscalated(), ticket.getCreatedAt(), ticket.getUpdatedAt()
        );
    }
}
