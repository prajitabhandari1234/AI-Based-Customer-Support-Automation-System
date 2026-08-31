package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
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
 *
 * <p>
 * The service also provides secure customer-specific ticket creation,
 * where the customer identity is obtained from JWT authentication
 * rather than being supplied directly by the client.
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
     * Creates a support ticket for an authenticated customer.
     *
     * <p>
     * The customer is identified using the email address stored in the
     * authenticated JWT. The method verifies that the requested
     * conversation exists and belongs to the authenticated customer
     * before creating the ticket.
     * </p>
     *
     * <p>
     * New customer tickets are automatically created with an
     * {@link TicketStatus#OPEN} status and are initially left
     * unassigned. AI analysis and resolution information may be added
     * later during ticket processing.
     * </p>
     *
     * @param request information required to create the ticket
     * @param customerEmail email address of the authenticated customer
     * @return the newly created support ticket
     * @throws ResourceNotFoundException if the customer or conversation
     *         cannot be found
     * @throws ForbiddenOperationException if the conversation does not
     *   belong to the authenticated customer
     */
    public Ticket createCustomerTicket(
            CreateTicketRequest request,
            String customerEmail) {

        String normalizedEmail = customerEmail.trim().toLowerCase();

        User customer = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with email: "
                                        + normalizedEmail));

        Conversation conversation = conversationRepository
                .findById(request.getConversationId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found with ID: "
                                        + request.getConversationId()));

        if (!conversation.getCustomer()
                .getUserId()
                .equals(customer.getUserId())) {

            throw new ForbiddenOperationException(
                    "Conversation does not belong to the authenticated customer");
        }

        Ticket ticket = new Ticket();

        ticket.setConversation(conversation);
        ticket.setCustomer(customer);
        ticket.setAssignedAgent(null);
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(TicketStatus.OPEN);

        return ticketRepository.save(ticket);
    }

    /**
     * Creates or updates a ticket.
     *
     * <p>
     * Verifies that the referenced conversation and customer exist.
     * If an assigned support agent is provided, that user is also
     * verified before the ticket is saved.
     * </p>
     *
     * @param ticket the ticket to be saved
     * @return the saved ticket
     * @throws ResourceNotFoundException if the conversation, customer,
     *         or assigned agent does not exist
     */
    public Ticket saveTicket(Ticket ticket) {

        Long conversationId =
                ticket.getConversation().getConversationId();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found with ID: "
                                        + conversationId));

        ticket.setConversation(conversation);

        Long customerId = ticket.getCustomer().getUserId();

        User customer = userRepository
                .findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with ID: "
                                        + customerId));

        ticket.setCustomer(customer);

        if (ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getUserId() != null) {

            Long assignedAgentId =
                    ticket.getAssignedAgent().getUserId();

            User assignedAgent = userRepository
                    .findById(assignedAgentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Assigned agent not found with ID: "
                                            + assignedAgentId));

            ticket.setAssignedAgent(assignedAgent);
        }

        return ticketRepository.save(ticket);
    }

    /**
     * Retrieves all support tickets.
     *
     * @return list containing all tickets
     */
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Retrieves a support ticket by its unique identifier.
     *
     * @param ticketId unique identifier of the ticket
     * @return ticket associated with the specified identifier
     * @throws ResourceNotFoundException if the ticket cannot be found
     */
    public Ticket getTicketById(Long ticketId) {

        return ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId));
    }

    /**
     * Deletes a support ticket by its identifier.
     *
     * @param ticketId identifier of the ticket to delete
     */
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}