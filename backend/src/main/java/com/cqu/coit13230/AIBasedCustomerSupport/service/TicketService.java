package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.MessageRepository;
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
 * The service also provides secure customer-specific ticket operations,
 * including ticket creation, ticket history retrieval, and detailed
 * ticket views with associated conversation message history.
 * Customer identity is obtained from JWT authentication rather than
 * being supplied directly by the client.
 * </p>
 */
@Service
public class TicketService {

    /**
     * Repository used to access support ticket records.
     */
    private final TicketRepository ticketRepository;

    /**
     * Repository used to access customer conversations.
     */
    private final ConversationRepository conversationRepository;

    /**
     * Repository used to access user records.
     */
    private final UserRepository userRepository;

    /**
     * Repository used to access conversation messages.
     */
    private final MessageRepository messageRepository;

    /**
     * Constructs a new {@code TicketService} with the required
     * repository dependencies.
     *
     * @param ticketRepository repository used to access ticket data
     * @param conversationRepository repository used to access conversation data
     * @param userRepository repository used to access user data
     * @param messageRepository repository used to access message data
     */
    public TicketService(
            TicketRepository ticketRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            MessageRepository messageRepository) {

        this.ticketRepository = ticketRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
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
     *         belong to the authenticated customer
     */
    public Ticket createCustomerTicket(
            CreateTicketRequest request,
            String customerEmail) {

        String normalizedEmail = customerEmail
                .trim()
                .toLowerCase();

        User customer = userRepository
                .findByEmail(normalizedEmail)
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
     * Retrieves the support ticket history of an authenticated customer.
     *
     * <p>
     * The customer is identified using the email address obtained from
     * the authenticated JWT. Only tickets belonging to that customer
     * are returned, preventing customers from viewing tickets owned by
     * other users.
     * </p>
     *
     * <p>
     * Tickets are returned from the most recently created ticket to
     * the oldest ticket.
     * </p>
     *
     * @param customerEmail email address of the authenticated customer
     * @return list of support tickets belonging to the customer
     * @throws ResourceNotFoundException if the authenticated customer
     *         cannot be found
     */
    public List<Ticket> getCustomerTicketHistory(
            String customerEmail) {

        String normalizedEmail = customerEmail
                .trim()
                .toLowerCase();

        User customer = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with email: "
                                        + normalizedEmail));

        return ticketRepository
                .findByCustomerUserIdOrderByCreatedAtDesc(
                        customer.getUserId());
    }

    /**
     * Retrieves detailed ticket information and the associated
     * conversation history for an authenticated customer.
     *
     * <p>
     * The customer is identified using the email address stored in the
     * authenticated JWT. The requested ticket is retrieved and ownership
     * is verified before any ticket or conversation information is
     * returned.
     * </p>
     *
     * <p>
     * If the ticket belongs to the authenticated customer, all messages
     * belonging to the ticket's conversation are retrieved in
     * chronological order from oldest to newest.
     * </p>
     *
     * @param ticketId unique identifier of the requested ticket
     * @param customerEmail email address of the authenticated customer
     * @return ticket details together with conversation message history
     * @throws ResourceNotFoundException if the customer or ticket
     *         cannot be found
     * @throws ForbiddenOperationException if the ticket does not belong
     *         to the authenticated customer
     */
    public TicketDetailsResponse getCustomerTicketDetails(
            Long ticketId,
            String customerEmail) {

        String normalizedEmail = customerEmail
                .trim()
                .toLowerCase();

        User customer = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with email: "
                                        + normalizedEmail));

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId));

        if (!ticket.getCustomer()
                .getUserId()
                .equals(customer.getUserId())) {

            throw new ForbiddenOperationException(
                    "Ticket does not belong to the authenticated customer");
        }

        Long conversationId =
                ticket.getConversation().getConversationId();

        List<Message> messages =
                messageRepository
                        .findByConversationConversationIdOrderByCreatedAtAsc(
                                conversationId);

        return new TicketDetailsResponse(
                ticket,
                messages);
    }

    /**
     * Creates or updates a support ticket.
     *
     * <p>
     * Verifies that the referenced conversation and customer exist.
     * If an assigned support agent is provided, that user is also
     * verified before the ticket is saved.
     * </p>
     *
     * @param ticket ticket to be saved
     * @return saved ticket
     * @throws ResourceNotFoundException if the conversation, customer,
     *         or assigned agent cannot be found
     */
    public Ticket saveTicket(Ticket ticket) {

        Long conversationId =
                ticket.getConversation()
                        .getConversationId();

        Conversation conversation =
                conversationRepository
                        .findById(conversationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conversation not found with ID: "
                                                + conversationId));

        ticket.setConversation(conversation);

        Long customerId =
                ticket.getCustomer()
                        .getUserId();

        User customer =
                userRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with ID: "
                                                + customerId));

        ticket.setCustomer(customer);

        if (ticket.getAssignedAgent() != null
                && ticket.getAssignedAgent().getUserId() != null) {

            Long assignedAgentId =
                    ticket.getAssignedAgent()
                            .getUserId();

            User assignedAgent =
                    userRepository
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
     * @return list containing all support tickets
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