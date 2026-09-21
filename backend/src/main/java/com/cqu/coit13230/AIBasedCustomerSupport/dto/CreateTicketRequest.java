package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries the details needed to create a support ticket.
 *
 * <p>
 * This Data Transfer Object (DTO) contains the information submitted
 * when creating a new support ticket within the customer support system.
 * </p>
 *
 * <p>
 * The request can include an associated conversation identifier,
 * ticket category, ticket priority, title, and customer message.
 * The DTO transfers the request data into the service layer for
 * ticket creation and processing.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    /**
     * Identifier of the conversation associated with the support ticket.
     *
     * <p>
     * This value can be used to link the newly created ticket to an
     * existing customer conversation.
     * </p>
     */
    private Long conversationId;

    /**
     * Category assigned to the support ticket.
     *
     * <p>
     * The category identifies the type of customer support issue
     * represented by the ticket.
     * </p>
     */
    private TicketCategory category;

    /**
     * Priority level assigned to the support ticket.
     *
     * <p>
     * The priority represents the level of attention associated
     * with the ticket.
     * </p>
     */
    private TicketPriority priority;

    /**
     * Title of the support ticket.
     *
     * <p>
     * This field provides a short description or heading for
     * the customer's support request.
     * </p>
     */
    private String title;

    /**
     * Message submitted as part of the support ticket.
     *
     * <p>
     * This field contains the customer's support request or
     * additional information associated with the ticket.
     * </p>
     */
    private String message;

}