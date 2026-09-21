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
 *
 * <p>
 * This Data Transfer Object (DTO) combines the main support ticket
 * information, ticket summary, escalation and resolution information,
 * response timestamps, and the messages associated with the ticket.
 * </p>
 *
 * <p>
 * The DTO also provides JSON property aliases for selected ticket
 * properties so that important ticket information can be exposed
 * directly in the API response while keeping the response structure
 * separate from the database entities.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailsResponse {

    /**
     * Complete ticket associated with this response.
     */
    private Ticket ticket;

    /**
     * Summary information generated from the ticket.
     */
    private TicketSummaryResponse summary;

    /**
     * Reason recorded for escalating the ticket.
     */
    private String escalationReason;

    /**
     * Resolution notes recorded for the ticket.
     */
    private String resolutionNotes;

    /**
     * Date and time when the first response was recorded for the ticket.
     */
    private LocalDateTime firstResponseAt;

    /**
     * Date and time when the ticket was resolved.
     */
    private LocalDateTime resolvedAt;

    /**
     * Date and time when the ticket was closed.
     */
    private LocalDateTime closedAt;

    /**
     * List of messages associated with the ticket.
     */
    private List<Message> messages;

    /**
     * Combines the ticket and its messages into one frontend response.
     *
     * <p>
     * This factory method creates a {@code TicketDetailsResponse} using
     * the supplied ticket and message list. It also creates the ticket
     * summary and copies the escalation, resolution, and timestamp
     * information from the supplied ticket.
     * </p>
     *
     * @param ticket   ticket whose details are included in the response
     * @param messages messages associated with the ticket
     * @return a complete ticket details response containing the ticket,
     *         summary, related details, and messages
     */
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

    /**
     * Returns the ticket identifier using the JSON property name
     * {@code id}.
     *
     * @return the ticket identifier, or {@code null} if no ticket is present
     */
    @JsonProperty("id")
    public Long getIdAlias() {

        return ticket == null ? null : ticket.getTicketId();

    }

    /**
     * Returns the ticket identifier using the JSON property name
     * {@code ticketId}.
     *
     * @return the ticket identifier, or {@code null} if no ticket is present
     */
    @JsonProperty("ticketId")
    public Long getTicketIdAlias() {

        return ticket == null ? null : ticket.getTicketId();

    }

    /**
     * Returns the title of the ticket using the JSON property name
     * {@code title}.
     *
     * @return the ticket title, or {@code null} if no ticket is present
     */
    @JsonProperty("title")
    public String getTitleAlias() {

        return ticket == null ? null : ticket.getTitle();

    }

    /**
     * Returns the customer associated with the ticket using the JSON
     * property name {@code customer}.
     *
     * @return the ticket customer, or {@code null} if no ticket is present
     */
    @JsonProperty("customer")
    public User getCustomerAlias() {

        return ticket == null ? null : ticket.getCustomer();

    }

    /**
     * Returns the support agent assigned to the ticket using the JSON
     * property name {@code assignedAgent}.
     *
     * @return the assigned agent, or {@code null} if no ticket is present
     */
    @JsonProperty("assignedAgent")
    public User getAssignedAgentAlias() {

        return ticket == null ? null : ticket.getAssignedAgent();

    }

    /**
     * Returns the category of the ticket using the JSON property name
     * {@code category}.
     *
     * @return the ticket category, or {@code null} if no ticket is present
     */
    @JsonProperty("category")
    public TicketCategory getCategoryAlias() {

        return ticket == null ? null : ticket.getCategory();

    }

    /**
     * Returns the priority of the ticket using the JSON property name
     * {@code priority}.
     *
     * @return the ticket priority, or {@code null} if no ticket is present
     */
    @JsonProperty("priority")
    public TicketPriority getPriorityAlias() {

        return ticket == null ? null : ticket.getPriority();

    }

    /**
     * Returns the current ticket status using the JSON property name
     * {@code status}.
     *
     * @return the ticket status, or {@code null} if no ticket is present
     */
    @JsonProperty("status")
    public TicketStatus getStatusAlias() {

        return ticket == null ? null : ticket.getStatus();

    }

    /**
     * Returns the sentiment score associated with the ticket using the
     * JSON property name {@code sentimentScore}.
     *
     * @return the ticket sentiment score, or {@code null} if no ticket
     *         is present
     */
    @JsonProperty("sentimentScore")
    public Double getSentimentScoreAlias() {

        return ticket == null ? null : ticket.getSentimentScore();

    }

    /**
     * Returns the AI confidence score associated with the ticket using
     * the JSON property name {@code aiConfidenceScore}.
     *
     * @return the AI confidence score, or {@code null} if no ticket
     *         is present
     */
    @JsonProperty("aiConfidenceScore")
    public Double getAiConfidenceScoreAlias() {

        return ticket == null ? null : ticket.getAiConfidenceScore();

    }

}