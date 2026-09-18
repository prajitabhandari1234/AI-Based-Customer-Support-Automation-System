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
 *
 * <p>
 * This Data Transfer Object (DTO) provides a simplified representation
 * of a support ticket for use in ticket lists and summary views.
 * </p>
 *
 * <p>
 * The response contains important ticket information such as the ticket
 * identifier, customer and assigned agent details, category, priority,
 * status, sentiment information, AI confidence score, escalation state,
 * and timestamps. The DTO keeps API response data separate from the
 * database entities.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketSummaryResponse {

    /**
     * General identifier used for the ticket summary response.
     */
    private Long id;

    /**
     * Unique identifier of the support ticket.
     */
    private Long ticketId;

    /**
     * Title of the support ticket.
     */
    private String title;

    /**
     * Unique identifier of the customer associated with the ticket.
     */
    private Long customerId;

    /**
     * Name of the customer associated with the ticket.
     */
    private String customerName;

    /**
     * Unique identifier of the support agent assigned to the ticket.
     *
     * <p>
     * This value may be {@code null} when no support agent has been
     * assigned to the ticket.
     * </p>
     */
    private Long assignedAgentId;

    /**
     * Name of the support agent assigned to the ticket.
     *
     * <p>
     * This value may be {@code null} when no support agent has been
     * assigned to the ticket.
     * </p>
     */
    private String assignedAgentName;

    /**
     * Category assigned to the support ticket.
     */
    private TicketCategory category;

    /**
     * Priority level assigned to the support ticket.
     */
    private TicketPriority priority;

    /**
     * Current status of the support ticket.
     */
    private TicketStatus status;

    /**
     * Sentiment classification associated with the support ticket.
     */
    private Sentiment sentiment;

    /**
     * Numerical sentiment score associated with the support ticket.
     */
    private Double sentimentScore;

    /**
     * AI confidence score associated with the support ticket.
     */
    private Double aiConfidenceScore;

    /**
     * Indicates whether the support ticket has been escalated.
     */
    private boolean escalated;

    /**
     * Date and time when the support ticket was created.
     */
    private LocalDateTime createdAt;

    /**
     * Date and time when the support ticket was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Maps a ticket entity to the smaller list response.
     *
     * <p>
     * This factory method extracts the required summary information
     * from the supplied {@link Ticket}. If no support agent is assigned
     * to the ticket, the assigned agent identifier and name are set
     * to {@code null}.
     * </p>
     *
     * @param ticket ticket entity to convert into a summary response
     * @return a ticket summary response containing the selected
     *         information from the supplied ticket
     */
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