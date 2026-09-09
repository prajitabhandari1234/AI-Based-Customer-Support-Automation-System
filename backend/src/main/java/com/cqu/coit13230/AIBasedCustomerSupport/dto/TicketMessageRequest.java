package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a new message submitted to an existing ticket.
 * The DTO is used to validate and transfer request data into the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageRequest {

    private String message;
    private String content;

    public String text() {
        if (message != null && !message.isBlank()) {
            return message;
        }
        return content;
    }
}
