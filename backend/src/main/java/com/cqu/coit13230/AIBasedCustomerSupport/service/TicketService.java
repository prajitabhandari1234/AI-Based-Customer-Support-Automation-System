package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentTicketUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.MessageRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link Ticket} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, escalating, assigning, and managing customer support tickets.
 * </p>
 *
 * <p>
 * The service also provides secure customer-specific and
 * support-agent-specific ticket operations. Customer and support-agent
 * identities are obtained from JWT authentication rather than being
 * supplied directly by the client.
 * </p>
 *
 * <p>
 * Ticket-related events may generate notifications for customers
 * and support agents through the {@link NotificationService}.
 * Important ticket lifecycle events are also recorded through
 * {@link SystemLogService} for system auditing.
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
     * Service used to generate ticket-related notifications.
     */
    private final NotificationService notificationService;

    /**
     * Service used to record ticket-related system activity.
     */
    private final SystemLogService systemLogService;

    /**
     * Constructs a new {@code TicketService} with the required
     * repository and service dependencies.
     *
     * @param ticketRepository repository used to access ticket data
     * @param conversationRepository repository used to access conversation data
     * @param userRepository repository used to access user data
     * @param messageRepository repository used to access message data
     * @param notificationService service used to generate ticket notifications
     * @param systemLogService service used to record ticket-related system events
     */
    public TicketService(
            TicketRepository ticketRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            MessageRepository messageRepository,
            NotificationService notificationService,
            SystemLogService systemLogService) {

        this.ticketRepository = ticketRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.notificationService = notificationService;
        this.systemLogService = systemLogService;
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

        Ticket savedTicket =
                ticketRepository.save(ticket);

        /*
         * Record the successful creation of the support ticket
         * after persistence so the generated ticket ID is available.
         */
        systemLogService.logTicketCreated(savedTicket);

        return savedTicket;
    }

    /**
     * Retrieves the support ticket history of an authenticated customer.
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
     * The referenced conversation, customer, and optionally assigned
     * support agent are verified before the ticket is persisted.
     * </p>
     *
     * @param ticket ticket to be saved
     * @return saved ticket
     * @throws ResourceNotFoundException if referenced entities cannot be found
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
     * Escalates an open support ticket for human assistance and
     * notifies all active support agents.
     *
     * <p>
     * Only a ticket currently in {@link TicketStatus#OPEN} status
     * can be escalated. After successful escalation, the ticket is
     * changed to {@link TicketStatus#ESCALATED} and persisted.
     * </p>
     *
     * <p>
     * The first escalation time is recorded using the
     * {@code escalatedAt} lifecycle timestamp. This allows analytics
     * to determine whether a ticket was historically escalated even
     * after the ticket later moves to another status.
     * </p>
     *
     * <p>
     * Every active user with the {@link UserRole#SUPPORT_AGENT}
     * role receives an unread notification informing them that
     * the escalated ticket requires human assistance.
     * </p>
     *
     * <p>
     * This method is intended to be called by backend escalation
     * workflows, including AI-based escalation decisions and
     * business-rule-based escalation.
     * </p>
     *
     * @param ticketId unique identifier of the ticket to escalate
     * @return the escalated support ticket
     * @throws ResourceNotFoundException if the ticket cannot be found
     * @throws ForbiddenOperationException if the ticket is not open
     */
    public Ticket escalateTicket(Long ticketId) {

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId));

        if (ticket.getStatus() != TicketStatus.OPEN) {

            throw new ForbiddenOperationException(
                    "Only open tickets can be escalated");
        }

        ticket.setStatus(TicketStatus.ESCALATED);

        /*
         * Record the first time the ticket is escalated.
         * The original timestamp is preserved for historical
         * escalation-rate analytics.
         */
        if (ticket.getEscalatedAt() == null) {

            ticket.setEscalatedAt(
                    LocalDateTime.now());
        }

        Ticket savedTicket =
                ticketRepository.save(ticket);

        /*
         * Record successful ticket escalation for system auditing.
         */
        systemLogService.logTicketEscalated(savedTicket);

        List<User> activeSupportAgents =
                userRepository.findByRoleAndStatus(
                        UserRole.SUPPORT_AGENT,
                        UserStatus.ACTIVE);

        for (User agent : activeSupportAgents) {

            notificationService.createTicketNotification(
                    agent,
                    savedTicket,
                    "New escalated ticket #"
                            + savedTicket.getTicketId()
                            + " requires human assistance.");
        }

        return savedTicket;
    }

    /**
     * Retrieves all support tickets that have been escalated for
     * human assistance.
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
     * The support agent is identified from JWT authentication.
     * Only tickets with {@link TicketStatus#ESCALATED} status may
     * be assigned.
     * </p>
     *
     * <p>
     * Following successful assignment, the ticket is moved to
     * {@link TicketStatus#IN_PROGRESS} and the customer receives
     * an unread notification informing them of the assignment.
     * </p>
     *
     * @param ticketId unique identifier of the ticket to assign
     * @param agentEmail email address of the authenticated support agent
     * @return updated ticket containing the assigned support agent
     * @throws ResourceNotFoundException if the agent or ticket cannot be found
     * @throws ForbiddenOperationException if assignment is not permitted
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

        Ticket savedTicket =
                ticketRepository.save(ticket);

        notificationService.createTicketNotification(
                savedTicket.getCustomer(),
                savedTicket,
                "A support agent has been assigned to your ticket.");

        return savedTicket;
    }

    /**
     * Updates the status and resolution information of a ticket
     * assigned to the authenticated support agent.
     *
     * <p>
     * Only the support agent currently assigned to the ticket is
     * permitted to perform the update. Supported status changes include
     * {@link TicketStatus#IN_PROGRESS},
     * {@link TicketStatus#ON_HOLD},
     * {@link TicketStatus#RESOLVED}, and
     * {@link TicketStatus#CLOSED}.
     * </p>
     *
     * <p>
     * Resolution notes are required when a ticket is moved to
     * {@link TicketStatus#RESOLVED} or {@link TicketStatus#CLOSED}.
     * The first resolution time is recorded in {@code resolvedAt}
     * for accurate resolution-time analytics.
     * </p>
     *
     * <p>
     * After the ticket is successfully updated, an unread notification
     * is generated for the customer informing them of the status change.
     * </p>
     *
     * @param ticketId unique identifier of the ticket
     * @param request updated ticket status and resolution information
     * @param agentEmail email address of the authenticated support agent
     * @return updated support ticket
     * @throws ResourceNotFoundException if the agent or ticket cannot be found
     * @throws ForbiddenOperationException if the ticket cannot be updated
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

        /*
         * Resolution notes are mandatory when resolving
         * or closing a support ticket.
         */
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

        /*
         * Record the first time the issue reaches a resolved
         * or closed state. The timestamp is preserved if the
         * ticket is later updated again.
         */
        if ((newStatus == TicketStatus.RESOLVED
                || newStatus == TicketStatus.CLOSED)
                && ticket.getResolvedAt() == null) {

            ticket.setResolvedAt(
                    LocalDateTime.now());
        }

        Ticket savedTicket =
                ticketRepository.save(ticket);

        /*
         * Determines the customer notification message based on the
         * newly updated ticket status.
         */
        String notificationMessage = switch (newStatus) {

            case IN_PROGRESS ->
                    "Your ticket is now being worked on.";

            case ON_HOLD ->
                    "Your ticket has been placed on hold.";

            case RESOLVED ->
                    "Your ticket has been resolved.";

            case CLOSED ->
                    "Your ticket has been closed.";

            default -> null;
        };

        /*
         * Generates an unread notification for the customer after
         * the ticket status has been successfully updated.
         */
        if (notificationMessage != null) {

            notificationService.createTicketNotification(
                    savedTicket.getCustomer(),
                    savedTicket,
                    notificationMessage);
        }

        return savedTicket;
    }

    /**
     * Sends a response from the authenticated support agent to the
     * conversation associated with an assigned support ticket.
     *
     * <p>
     * The support agent is identified using the email address obtained
     * from JWT authentication. Only the support agent currently assigned
     * to the ticket is permitted to send a response.
     * </p>
     *
     * <p>
     * The response is stored as a new conversation message with
     * {@link SenderType#SUPPORT_AGENT} as its sender type. After the
     * message is successfully saved, the customer receives an unread
     * notification informing them that a new support-agent response
     * is available.
     * </p>
     *
     * @param ticketId unique identifier of the support ticket
     * @param request request containing the support agent's response
     * @param agentEmail email address of the authenticated support agent
     * @return newly created support-agent message
     * @throws ResourceNotFoundException if the support agent or ticket
     *         cannot be found
     * @throws ForbiddenOperationException if the ticket is not assigned
     *         to the authenticated support agent
     */
    public Message sendAgentResponse(
            Long ticketId,
            AgentMessageRequest request,
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
                    "Only support agents can send ticket responses");
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

        Message message = new Message();

        message.setConversation(ticket.getConversation());
        message.setSenderUser(agent);
        message.setSenderType(SenderType.SUPPORT_AGENT);
        message.setContent(request.getContent().trim());
        message.setSentimentScore(null);

        Message savedMessage =
                messageRepository.save(message);

        /*
         * Notifies the customer that a support agent has sent
         * a new response regarding the ticket.
         */
        notificationService.createTicketNotification(
                ticket.getCustomer(),
                ticket,
                "You have received a new response from a support agent.");

        return savedMessage;
    }
}