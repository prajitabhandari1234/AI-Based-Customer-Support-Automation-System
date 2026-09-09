package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.time.LocalDateTime;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the smaller ticket view used in ticket lists.
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketSummaryResponse {

    private Long id;
    private Long ticketId;
    private String title;
    private Long customerId;
    private String customerName;
    private Long assignedAgentId;
    private String assignedAgentName;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private Sentiment sentiment;
    private Double sentimentScore;
    private Double aiConfidenceScore;
    private boolean escalated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Maps a ticket entity to the smaller list response.
    public static TicketSummaryResponse from(Ticket ticket) {
        Long agentId = ticket.getAssignedAgent() == null ? null : ticket.getAssignedAgent().getUserId();
        String agentName = ticket.getAssignedAgent() == null ? null : ticket.getAssignedAgent().getName();

        return new TicketSummaryResponse(
                ticket.getTicketId(),
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getCustomer().getUserId(),
                ticket.getCustomer().getName(),
                agentId,
                agentName,
                ticket.getCategory(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getSentiment(),
                ticket.getSentimentScore(),
                ticket.getAiConfidenceScore(),
                ticket.isEscalated(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}
