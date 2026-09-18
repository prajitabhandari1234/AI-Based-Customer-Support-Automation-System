package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a ticket status change from the frontend.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * ticket status updates submitted from the frontend of the customer
 * support system.
 * </p>
 *
 * <p>
 * The request contains the new ticket status and optional resolution
 * notes. The validated request data is transferred to the service layer,
 * where the ticket update is processed.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusRequest {

    /**
     * New status to be assigned to the support ticket.
     *
     * <p>
     * The status is required and must contain a valid
     * {@link TicketStatus} value.
     * </p>
     */
    @NotNull
    private TicketStatus status;

    /**
     * Optional resolution notes associated with the ticket status change.
     *
     * <p>
     * These notes can provide additional information about the
     * resolution of the support ticket when applicable.
     * </p>
     */
    private String resolutionNotes;

}