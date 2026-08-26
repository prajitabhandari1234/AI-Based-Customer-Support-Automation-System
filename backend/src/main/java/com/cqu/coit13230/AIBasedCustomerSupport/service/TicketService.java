package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link Ticket} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting customer support tickets.
 * </p>
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new {@code TicketService} with the required
     * repository dependencies.
     *
     * @param ticketRepository repository used to access ticket data
     * @param conversationRepository repository used to access conversation data
     * @param userRepository repository used to access user data
     */
    public TicketService(
            TicketRepository ticketRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository) {

        this.ticketRepository = ticketRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates or updates a ticket.
     *
     * <p>
     * Verifies that the referenced conversation and customer exist.
     * If an assigned support agent is provided, that user is also verified
     * before the ticket is saved.
     * </p>
     *
     * @param ticket the ticket to be saved
     * @return the saved ticket
     * @throws ResourceNotFoundException if the conversation, customer,
     *         or assigned agent does not exist
     */
    public Ticket saveTicket(Ticket ticket) {

        Long conversationId = ticket.getConversation().getConversationId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found with ID: " + conversationId));

        ticket.setConversation(conversation);

        Long customerId = ticket.getCustomer().getUserId();

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: " + customerId));

        ticket.setCustomer(customer);

        if (ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getUserId() != null) {

            Long assignedAgentId = ticket.getAssignedAgent().getUserId();

            User assignedAgent = userRepository.findById(assignedAgentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Assigned agent not found with ID: "
                                            + assignedAgentId));

            ticket.setAssignedAgent(assignedAgent);
        }

        return ticketRepository.save(ticket);
    }

    /**
     * Retrieves all tickets.
     *
     * @return a list of all tickets
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Retrieves a ticket by its unique identifier.
     *
     * @param ticketId the unique identifier of the ticket
     * @return the ticket associated with the specified identifier
     * @throws ResourceNotFoundException if no ticket exists with the specified identifier
     */
    public Ticket getTicketById(Long ticketId) {

        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: " + ticketId));
    }

    /**
     * Deletes a ticket by identifier.
     *
     * @param ticketId the identifier of the ticket to delete
     */
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}