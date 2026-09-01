package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) used when a support agent updates
 * the status and resolution information of an assigned support ticket.
 *
 * <p>
 * This request allows a support agent to change the lifecycle status
 * of a ticket and optionally provide resolution notes describing
 * the action taken to address the customer's issue.
 * </p>
 *
 * <p>
 * Resolution notes may be required by the service layer when a ticket
 * is moved to a resolved or closed state.
 * </p>
 */
public class AgentTicketUpdateRequest {

    /**
     * The new lifecycle status to be assigned to the support ticket.
     *
     * <p>
     * This field is mandatory and must contain a valid
     * {@link TicketStatus} value.
     * </p>
     */
    @NotNull(message = "Ticket status is required")
    private TicketStatus status;

    /**
     * Notes provided by the support agent describing the resolution
     * or actions taken for the support ticket.
     *
     * <p>
     * The value is optional for general status updates and must not
     * exceed 5000 characters.
     * </p>
     */
    @Size(
            max = 5000,
            message = "Resolution notes must not exceed 5000 characters"
    )
    private String resolutionNotes;

    /**
     * Returns the requested lifecycle status of the support ticket.
     *
     * @return the requested ticket status
     */
    public TicketStatus getStatus() {
        return status;
    }

    /**
     * Sets the requested lifecycle status of the support ticket.
     *
     * @param status the ticket status to set
     */
    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    /**
     * Returns the resolution notes provided by the support agent.
     *
     * @return the resolution notes, or {@code null} if none were provided
     */
    public String getResolutionNotes() {
        return resolutionNotes;
    }

    /**
     * Sets the resolution notes for the support ticket.
     *
     * @param resolutionNotes the resolution notes to set
     */
    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}