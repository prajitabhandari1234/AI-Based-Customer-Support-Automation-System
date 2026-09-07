package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;

import jakarta.validation.constraints.NotNull;

/**
 * Represents the information required by a customer to create
 * a new support ticket.
 *
 * <p>
 * The request contains only information that a customer is permitted
 * to provide. The authenticated customer's identity is obtained from
 * the JWT authentication context rather than from the request body.
 * </p>
 *
 * <p>
 * Ticket status, assigned support agent, AI confidence score,
 * sentiment score, and resolution information are managed internally
 * by the backend.
 * </p>
 */
public class CreateTicketRequest {

    /**
     * Identifier of the conversation from which the ticket originates.
     */
    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    /**
     * Category describing the type of customer support issue.
     */
    @NotNull(message = "Ticket category is required")
    private TicketCategory category;

    /**
     * Priority indicating the urgency of the support issue.
     */
    @NotNull(message = "Ticket priority is required")
    private TicketPriority priority;

    /**
     * Constructs an empty {@code CreateTicketRequest}.
     */
    public CreateTicketRequest() {
    }

    /**
     * Constructs a new {@code CreateTicketRequest}.
     *
     * @param conversationId identifier of the related conversation
     * @param category category of the support issue
     * @param priority priority of the support ticket
     */
    public CreateTicketRequest(
            Long conversationId,
            TicketCategory category,
            TicketPriority priority) {

        this.conversationId = conversationId;
        this.category = category;
        this.priority = priority;
    }

    /**
     * Returns the conversation identifier.
     *
     * @return conversation identifier
     */
    public Long getConversationId() {
        return conversationId;
    }

    /**
     * Sets the conversation identifier.
     *
     * @param conversationId conversation identifier
     */
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    /**
     * Returns the ticket category.
     *
     * @return ticket category
     */
    public TicketCategory getCategory() {
        return category;
    }

    /**
     * Sets the ticket category.
     *
     * @param category ticket category
     */
    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    /**
     * Returns the ticket priority.
     *
     * @return ticket priority
     */
    public TicketPriority getPriority() {
        return priority;
    }

    /**
     * Sets the ticket priority.
     *
     * @param priority ticket priority
     */
    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}