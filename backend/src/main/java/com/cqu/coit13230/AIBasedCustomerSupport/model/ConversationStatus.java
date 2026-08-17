package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Defines the possible states of a customer support conversation.
 *
 * <p>A conversation may be active, completed successfully, or escalated
 * to a human support agent when automated support is insufficient.</p>
 */
public enum ConversationStatus {

    /** Indicates that the conversation is currently in progress. */
    ACTIVE,

    /** Indicates that the conversation has been completed. */
    COMPLETED,

    /** Indicates that the conversation has been escalated to human support. */
    ESCALATED
}