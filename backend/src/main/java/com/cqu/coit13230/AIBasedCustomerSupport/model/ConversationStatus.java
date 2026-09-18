package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Represents the possible states of a customer support conversation
 * within the AI-based customer support system.
 *
 * <p>
 * A conversation can be active while support is in progress,
 * completed when the interaction has finished, or escalated when
 * further support is required.
 * </p>
 */
public enum ConversationStatus {

    /**
     * Indicates that the customer support conversation is currently active.
     */
    ACTIVE,

    /**
     * Indicates that the customer support conversation has been completed.
     */
    COMPLETED,

    /**
     * Indicates that the customer support conversation has been escalated.
     */
    ESCALATED

}