package com.cqu.coit13230.AIBasedCustomerSupport.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Lists the roles available to system users.
 */
public enum UserRole {
    CLIENT,
    AGENT,
    ADMIN;

    // Accepts role labels in a case-insensitive API format.
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
