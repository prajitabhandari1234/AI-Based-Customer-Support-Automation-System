package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Defines the possible lifecycle states of a customer support ticket.
 *
 * <p>The status indicates the current stage of a ticket from its
 * creation through escalation, processing, resolution, and closure.</p>
 */
public enum TicketStatus {

    /** Indicates that the ticket has been created and is awaiting action. */
    OPEN,

    /** Indicates that the ticket has been escalated for human support. */
    ESCALATED,

    /** Indicates that a support agent is currently handling the ticket. */
    IN_PROGRESS,

    /** Indicates that processing of the ticket has been temporarily paused. */
    ON_HOLD,

    /** Indicates that the customer issue has been successfully resolved. */
    RESOLVED,

    /** Indicates that the ticket has been formally closed. */
    CLOSED
}