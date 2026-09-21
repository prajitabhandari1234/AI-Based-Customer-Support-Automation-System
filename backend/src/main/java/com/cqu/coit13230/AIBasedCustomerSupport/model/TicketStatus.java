package com.cqu.coit13230.AIBasedCustomerSupport.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents the possible states of a customer support ticket
 * within the AI-based customer support system.
 *
 * <p>
 * These status values describe the different stages a support ticket
 * can move through during its lifecycle, from initial creation through
 * escalation, processing, resolution, and closure.
 * </p>
 *
 * <p>
 * The enumeration also supports case-insensitive status values received
 * through the API and maps the alternative {@code NEW} status label
 * to {@link #OPEN}.
 * </p>
 */
public enum TicketStatus {

    /**
     * Indicates that the support ticket is open and awaiting processing.
     */
    OPEN,

    /**
     * Indicates that the support ticket has been escalated for
     * additional attention or human support.
     */
    ESCALATED,

    /**
     * Indicates that the support ticket is currently being processed.
     */
    IN_PROGRESS,

    /**
     * Indicates that processing of the support ticket has temporarily
     * been placed on hold.
     */
    ON_HOLD,

    /**
     * Indicates that the support ticket has been resolved by the
     * AI-based customer support system.
     */
    RESOLVED_BY_AI,

    /**
     * Indicates that the support ticket has been resolved.
     */
    RESOLVED,

    /**
     * Indicates that the support ticket has been closed.
     */
    CLOSED;

    /**
     * Converts a status label received through the API into the
     * corresponding {@code TicketStatus} value.
     *
     * <p>
     * If the supplied value is {@code null}, this method returns
     * {@code null}. The alternative status label {@code NEW} is mapped
     * to {@link #OPEN}. All other values are trimmed, converted to
     * uppercase, and matched with the corresponding enumeration value.
     * </p>
     *
     * @param value status label received through the API
     * @return the corresponding ticket status, or {@code null} if the
     *         supplied value is {@code null}
     */
    @JsonCreator
    public static TicketStatus fromValue(String value) {

        if (value == null) {

            return null;

        }

        if ("NEW".equalsIgnoreCase(value.trim())) {

            return OPEN;

        }

        return TicketStatus.valueOf(value.trim().toUpperCase());

    }

}