package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.util.List;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;

/**
 * Represents detailed information about a support ticket together
 * with the message history of its associated conversation.
 *
 * <p>
 * This response object is used when an authenticated customer views
 * an individual support ticket. It combines ticket information with
 * the complete chronological conversation history.
 * </p>
 */
public class TicketDetailsResponse {

    /**
     * Support ticket requested by the customer.
     */
    private Ticket ticket;

    /**
     * Messages belonging to the conversation associated with the ticket.
     */
    private List<Message> messages;

    /**
     * Constructs an empty {@code TicketDetailsResponse}.
     */
    public TicketDetailsResponse() {
    }

    /**
     * Constructs a new {@code TicketDetailsResponse}.
     *
     * @param ticket support ticket information
     * @param messages conversation messages associated with the ticket
     */
    public TicketDetailsResponse(
            Ticket ticket,
            List<Message> messages) {

        this.ticket = ticket;
        this.messages = messages;
    }

    /**
     * Returns the support ticket.
     *
     * @return support ticket information
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * Sets the support ticket.
     *
     * @param ticket support ticket information
     */
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Returns the conversation message history.
     *
     * @return ordered list of conversation messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Sets the conversation message history.
     *
     * @param messages conversation messages
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}