package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries ticket updates submitted by a support agent.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * ticket update information submitted by a support agent.
 * </p>
 *
 * <p>
 * The validated request data is transferred to the service layer,
 * where the ticket status and optional resolution notes are processed
 * for the relevant support ticket.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentTicketUpdateRequest {

    /**
     * Status to be assigned to the support ticket.
     *
     * <p>
     * The ticket status is required and must contain a valid
     * {@link TicketStatus} value.
     * </p>
     */
    @NotNull
    private TicketStatus status;

    /**
     * Optional resolution notes associated with the ticket update.
     *
     * <p>
     * These notes can provide additional information about how the
     * support ticket was handled or resolved.
     * </p>
     */
    private String resolutionNotes;

}