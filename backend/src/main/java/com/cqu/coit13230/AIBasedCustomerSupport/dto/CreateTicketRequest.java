package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries the details needed to create a support ticket.
 * The DTO is used to validate and transfer request data into the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    private Long conversationId;
    private TicketCategory category;
    private TicketPriority priority;
    private String title;
    private String message;
}
