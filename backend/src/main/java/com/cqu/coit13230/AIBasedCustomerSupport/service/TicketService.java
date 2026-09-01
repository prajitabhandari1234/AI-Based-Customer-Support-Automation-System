package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentTicketUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
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

        /**
         * Retrieves all support tickets that have been escalated for
         * human assistance.
         *
         * <p>
         * Escalated tickets are returned from the oldest to the newest so
         * that support agents can prioritise tickets that have been waiting
         * the longest.
         * </p>
         *
         * @return list of tickets with {@link TicketStatus#ESCALATED} status
         */
        public List<Ticket> getEscalatedTickets() {

        return ticketRepository
                .findByStatusOrderByCreatedAtAsc(
                        TicketStatus.ESCALATED);
        }

        /**
         * Assigns an escalated support ticket to the authenticated
         * support agent.
         *
         * <p>
         * The support agent is identified using the email address obtained
         * from JWT authentication. Only users with the
         * {@link UserRole#SUPPORT_AGENT} role can assign tickets.
         * </p>
         *
         * <p>
         * Only tickets with {@link TicketStatus#ESCALATED} status can be
         * assigned. After successful assignment, the ticket status is
         * changed to {@link TicketStatus#IN_PROGRESS}.
         * </p>
         *
         * @param ticketId unique identifier of the ticket to assign
         * @param agentEmail email address of the authenticated support agent
         * @return updated ticket containing the assigned support agent
         * @throws ResourceNotFoundException if the agent or ticket cannot be found
         * @throws ForbiddenOperationException if the user is not a support agent
         *         or the ticket cannot currently be assigned
         */
        public Ticket assignTicketToAgent(
                Long ticketId,
                String agentEmail) {

        String normalizedEmail = agentEmail
                .trim()
                .toLowerCase();

        User agent = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Support agent not found with email: "
                                        + normalizedEmail));

        if (agent.getRole() != UserRole.SUPPORT_AGENT) {
                throw new ForbiddenOperationException(
                        "Only support agents can assign tickets");
        }

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId));

        if (ticket.getStatus() != TicketStatus.ESCALATED) {
                throw new ForbiddenOperationException(
                        "Only escalated tickets can be assigned");
        }

        ticket.setAssignedAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        return ticketRepository.save(ticket);
        }

        /**
         * Updates the status and resolution information of a ticket
         * assigned to the authenticated support agent.
         *
         * <p>
         * The authenticated support agent is identified using the email
         * address obtained from JWT authentication. A support agent may
         * only update tickets currently assigned to them.
         * </p>
         *
         * <p>
         * Supported status transitions include
         * {@link TicketStatus#IN_PROGRESS},
         * {@link TicketStatus#ON_HOLD},
         * {@link TicketStatus#RESOLVED}, and
         * {@link TicketStatus#CLOSED}.
         * </p>
         *
         * @param ticketId unique identifier of the ticket
         * @param request updated ticket status and resolution information
         * @param agentEmail email address of the authenticated support agent
         * @return updated support ticket
         * @throws ResourceNotFoundException if the agent or ticket cannot be found
         * @throws ForbiddenOperationException if the ticket is not assigned
         *         to the authenticated support agent or the requested status
         *         is not permitted
         */
        public Ticket updateAssignedTicket(
                Long ticketId,
                AgentTicketUpdateRequest request,
                String agentEmail) {

        String normalizedEmail = agentEmail
                .trim()
                .toLowerCase();

        User agent = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Support agent not found with email: "
                                        + normalizedEmail));

        if (agent.getRole() != UserRole.SUPPORT_AGENT) {
                throw new ForbiddenOperationException(
                        "Only support agents can update assigned tickets");
        }

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId));

        if (ticket.getAssignedAgent() == null
                || !ticket.getAssignedAgent()
                        .getUserId()
                        .equals(agent.getUserId())) {

                throw new ForbiddenOperationException(
                        "Ticket is not assigned to the authenticated support agent");
        }

        TicketStatus newStatus = request.getStatus();

        if (newStatus != TicketStatus.IN_PROGRESS
                && newStatus != TicketStatus.ON_HOLD
                && newStatus != TicketStatus.RESOLVED
                && newStatus != TicketStatus.CLOSED) {

                throw new ForbiddenOperationException(
                        "Support agent cannot change ticket to status: "
                                + newStatus);
        }

        if ((newStatus == TicketStatus.RESOLVED
                || newStatus == TicketStatus.CLOSED)
                && (request.getResolutionNotes() == null
                || request.getResolutionNotes().isBlank())) {

                throw new IllegalArgumentException(
                        "Resolution notes are required when resolving or closing a ticket");
        }

        ticket.setStatus(newStatus);

        if (request.getResolutionNotes() != null
                && !request.getResolutionNotes().isBlank()) {

                ticket.setResolutionNotes(
                        request.getResolutionNotes().trim());
        }

        return ticketRepository.save(ticket);
        }
}