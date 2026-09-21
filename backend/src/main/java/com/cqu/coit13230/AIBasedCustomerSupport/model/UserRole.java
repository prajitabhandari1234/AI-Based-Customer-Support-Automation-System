package com.cqu.coit13230.AIBasedCustomerSupport.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents the roles available to users within the AI-based
 * customer support system.
 *
 * <p>
 * User roles define the type of access and responsibilities assigned
 * to each system user. A user can operate as a client, support agent,
 * or administrator.
 * </p>
 *
 * <p>
 * The enumeration also supports alternative role labels received
 * through the API by converting them into the corresponding internal
 * role values.
 * </p>
 */
public enum UserRole {

    /**
     * Represents a client who uses the system to request customer support.
     */
    CLIENT,

    /**
     * Represents a support agent who manages and responds to
     * customer support requests.
     */
    AGENT,

    /**
     * Represents an administrator with administrative access
     * within the system.
     */
    ADMIN;

    /**
     * Converts a role label received through the API into the
     * corresponding {@code UserRole} value.
     *
     * <p>
     * The supplied value is trimmed and converted to uppercase before
     * processing. The alternative label {@code CUSTOMER} is mapped to
     * {@link #CLIENT}, while {@code SUPPORT_AGENT} is mapped to
     * {@link #AGENT}. Other values are converted using the matching
     * enumeration value.
     * </p>
     *
     * @param value role label received through the API
     * @return the corresponding user role, or {@code null} if the
     *         supplied value is {@code null}
     */
    @JsonCreator
    public static UserRole fromValue(String value) {

        if (value == null) {

            return null;

        }

        return switch (value.trim().toUpperCase()) {

            case "CUSTOMER" -> CLIENT;

            case "SUPPORT_AGENT" -> AGENT;

            default -> UserRole.valueOf(value.trim().toUpperCase());

        };

    }

}