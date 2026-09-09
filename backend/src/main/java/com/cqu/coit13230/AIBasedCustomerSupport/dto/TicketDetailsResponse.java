package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns full ticket details together with its messages.
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailsResponse {

    private Ticket ticket;
    private TicketSummaryResponse summary;
    private String escalationReason;
    private String resolutionNotes;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private List<Message> messages;

    // Combines the ticket and its messages into one frontend response.
    public static TicketDetailsResponse from(Ticket ticket, List<Message> messages) {
        return new TicketDetailsResponse(
                ticket,
                TicketSummaryResponse.from(ticket),
                ticket.getEscalationReason(),
                ticket.getResolutionNotes(),
                ticket.getFirstResponseAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                messages);
    }

    @JsonProperty("id")
    public Long getIdAlias() {
        return ticket == null ? null : ticket.getTicketId();
    }

    @JsonProperty("ticketId")
    public Long getTicketIdAlias() {
        return ticket == null ? null : ticket.getTicketId();
    }

    @JsonProperty("title")
    public String getTitleAlias() {
        return ticket == null ? null : ticket.getTitle();
    }

    @JsonProperty("customer")
    public User getCustomerAlias() {
        return ticket == null ? null : ticket.getCustomer();
    }

    @JsonProperty("assignedAgent")
    public User getAssignedAgentAlias() {
        return ticket == null ? null : ticket.getAssignedAgent();
    }

    @JsonProperty("category")
    public TicketCategory getCategoryAlias() {
        return ticket == null ? null : ticket.getCategory();
    }

    @JsonProperty("priority")
    public TicketPriority getPriorityAlias() {
        return ticket == null ? null : ticket.getPriority();
    }

    @JsonProperty("status")
    public TicketStatus getStatusAlias() {
        return ticket == null ? null : ticket.getStatus();
    }

    @JsonProperty("sentimentScore")
    public Double getSentimentScoreAlias() {
        return ticket == null ? null : ticket.getSentimentScore();
    }

    @JsonProperty("aiConfidenceScore")
    public Double getAiConfidenceScoreAlias() {
        return ticket == null ? null : ticket.getAiConfidenceScore();
    }

}
