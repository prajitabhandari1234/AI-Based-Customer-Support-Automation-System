package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Defines the roles available to users within the AI-Based Customer
 * Support system.
 *
 * <p>Each role represents a different level of responsibility and
 * access within the system.</p>
 */
public enum UserRole {

    /**
     * Represents a customer who interacts with the system to receive support.
     */
    CUSTOMER,

    /**
     * Represents a support agent responsible for handling customer
     * support requests.
     */
    SUPPORT_AGENT,

    /**
     * Represents an administrator responsible for managing users
     * and system-level operations.
     */
    ADMIN
}