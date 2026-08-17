package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Defines the priority levels that can be assigned to support tickets.
 *
 * <p>Priority levels indicate the urgency of a customer issue and
 * assist support agents in determining the order in which tickets
 * should be handled.</p>
 */
public enum TicketPriority {

    /** Represents a low-priority issue requiring no immediate action. */
    LOW,

    /** Represents a standard-priority issue requiring normal attention. */
    MEDIUM,

    /** Represents a high-priority issue requiring prompt attention. */
    HIGH,

    /** Represents a critical issue requiring immediate attention. */
    CRITICAL
}