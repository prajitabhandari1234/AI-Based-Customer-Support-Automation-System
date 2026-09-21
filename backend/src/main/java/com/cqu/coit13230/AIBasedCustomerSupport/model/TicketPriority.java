package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Represents the priority levels that can be assigned to customer
 * support tickets within the AI-based customer support system.
 *
 * <p>
 * Ticket priority indicates the level of attention or urgency associated
 * with a support request. These priority levels can be used to organise,
 * manage, and process support tickets according to their importance.
 * </p>
 */
public enum TicketPriority {

    /**
     * Indicates a low-priority support ticket.
     */
    LOW,

    /**
     * Indicates a medium-priority support ticket.
     */
    MEDIUM,

    /**
     * Indicates a high-priority support ticket.
     */
    HIGH,

    /**
     * Indicates a critical-priority support ticket requiring the
     * highest level of attention.
     */
    CRITICAL

}