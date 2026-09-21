package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Represents the possible account states of a user within the
 * AI-based customer support system.
 *
 * <p>
 * The user status indicates whether a system account is currently
 * active and available for use or has been marked as inactive.
 * These states can be used to control whether a user is permitted
 * to access and interact with the system.
 * </p>
 */
public enum UserStatus {

    /**
     * Indicates that the user account is active and available for use.
     */
    ACTIVE,

    /**
     * Indicates that the user account is inactive.
     */
    INACTIVE

}