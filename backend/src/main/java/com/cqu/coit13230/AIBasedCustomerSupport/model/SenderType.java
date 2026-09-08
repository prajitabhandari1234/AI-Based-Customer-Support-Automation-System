package com.cqu.coit13230.AIBasedCustomerSupport.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Lists the possible senders of a conversation message.
 */
public enum SenderType {
    CLIENT,
    AI,
    AGENT,
    SYSTEM;

    // Accepts different sender labels used by the API.
    @JsonCreator
    public static SenderType fromValue(String value) {
        if (value == null) {
            return null;
        }

        return switch (value.trim().toUpperCase()) {
            case "CUSTOMER" -> CLIENT;
            case "SUPPORT_AGENT" -> AGENT;
            default -> SenderType.valueOf(value.trim().toUpperCase());
        };
    }
}
