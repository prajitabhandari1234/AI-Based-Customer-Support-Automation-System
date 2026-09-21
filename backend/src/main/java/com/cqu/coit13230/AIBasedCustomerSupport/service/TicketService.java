package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AgentTicketUpdateRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AiAnalysisResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.ChatResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.CreateTicketRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketDetailsResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketMessageRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketStatusRequest;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.TicketSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.BadRequestException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Conversation;
import com.cqu.coit13230.AIBasedCustomerSupport.model.ConversationStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Message;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SenderType;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.ConversationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.service.AiService.AiResult;

/**
 * Provides the core business logic for ticket and customer-chat workflows.
 *
 * <p>
 * This service manages ticket creation, AI-assisted customer conversations,
 * ticket escalation, support-agent assignment, ticket status updates,
 * customer and agent messages, notifications, system logging, and
 * role-based ticket access.
 * </p>
 */
@Service
public class TicketService {

    /** Repository used to persist and retrieve support tickets. */
    private final TicketRepository ticketRepository;

    /** Repository used to persist and retrieve customer conversations. */
    private final ConversationRepository conversationRepository;

    /** Repository used to retrieve users required by ticket workflows. */
    private final UserRepository userRepository;

    /** Service used to access and validate application users. */
    private final UserService userService;

    /** Service used to create and retrieve conversation messages. */
    private final MessageService messageService;

    /** Service used to create ticket-related notifications. */
    private final NotificationService notificationService;

    /** Service used to record important ticket and AI events. */
    private final SystemLogService systemLogService;

    /** Service used to analyse customer messages using AI. */
    private final AiService aiService;

    /**
     * Creates a ticket service with all dependencies required for ticket,
     * conversation, messaging, notification, logging, and AI workflows.
     *
     * @param ticketRepository       repository used to access tickets
     * @param conversationRepository repository used to access conversations
     * @param userRepository         repository used to access users
     * @param userService            service used for authenticated user operations
     * @param messageService         service used for message operations
     * @param notificationService    service used for notification operations
     * @param systemLogService       service used for system logging
     * @param aiService              service used for AI analysis
     */
    public TicketService(
            TicketRepository ticketRepository,
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            UserService userService,
            MessageService messageService,
            NotificationService notificationService,
            SystemLogService systemLogService,
            AiService aiService) {

        this.ticketRepository = ticketRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.systemLogService = systemLogService;
        this.aiService = aiService;
    }

    /**
     * Starts a new AI-assisted customer-support chat.
     *
     * <p>
     * The customer message is analysed before a support conversation or
     * ticket is created. Messages outside the customer-support scope are
     * rejected without creating persistent support records. Valid messages
     * create a conversation, customer message, AI-generated ticket, and
     * AI response.
     * </p>
     *
     * @param customerMessage initial customer message
     * @return chat response containing the AI reply and ticket information
     */
    @Transactional
    public ChatResponse startChat(String customerMessage) {
        User customer = requireCustomer(userService.currentUser());

        AiResult result = aiService.analyse(customerMessage);

        if (!result.inScope()) {
            return new ChatResponse(
                    result.reply(),
                    null,
                    null);
        }

        Conversation conversation = createConversation(customer);

        messageService.createMessage(
                conversation,
                customer,
                SenderType.CLIENT,
                customerMessage,
                result.sentiment(),
                result.sentimentScore());

        Ticket ticket = createAiTicket(
                conversation,
                customer,
                titleFrom(customerMessage),
                result);

        messageService.createMessage(
                conversation,
                null,
                SenderType.AI,
                result.reply(),
                Sentiment.NEUTRAL,
                0.0);

        ticket.setFirstResponseAt(LocalDateTime.now());
        finishConversationFromTicket(conversation, ticket);
        ticket = ticketRepository.save(ticket);
        conversationRepository.save(conversation);

        afterAiProcessing(ticket, result);
        return chatResponse(ticket, result);
    }

    /**
     * Continues an existing AI-assisted customer-support conversation.
     *
     * <p>
     * The customer message is analysed and, when it is within support
     * scope, stored in the existing conversation. The ticket is then
     * refreshed using the latest AI classification, sentiment, priority,
     * confidence, and escalation information.
     * </p>
     *
     * @param ticketId        identifier of the ticket being continued
     * @param customerMessage new customer message
     * @return updated chat response
     */
    @Transactional
    public ChatResponse continueChat(Long ticketId, String customerMessage) {
        User customer = requireCustomer(userService.currentUser());
        Ticket ticket = getTicketById(ticketId);
        assertCustomerOwnsTicket(ticket, customer);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("This ticket is closed");
        }

        AiResult result = aiService.analyse(customerMessage);

        if (!result.inScope()) {
            return new ChatResponse(
                    result.reply(),
                    TicketSummaryResponse.from(ticket),
                    null);
        }

        Conversation conversation = ticket.getConversation();
        if (conversation.getStatus() == ConversationStatus.COMPLETED) {
            conversation.setStatus(ConversationStatus.ACTIVE);
            conversation.setEndedAt(null);
        }

        messageService.createMessage(
                conversation,
                customer,
                SenderType.CLIENT,
                customerMessage,
                result.sentiment(),
                result.sentimentScore());

        TicketPriority previousPriority = ticket.getPriority();
        applyAiResult(ticket, result);
        ticket.setPriority(higherPriority(previousPriority, result.priority()));

        messageService.createMessage(
                conversation,
                null,
                SenderType.AI,
                result.reply(),
                Sentiment.NEUTRAL,
                0.0);

        if (ticket.getFirstResponseAt() == null) {
            ticket.setFirstResponseAt(LocalDateTime.now());
        }

        finishConversationFromTicket(conversation, ticket);
        ticket = ticketRepository.save(ticket);
        conversationRepository.save(conversation);

        if (ticket.isEscalated() && ticket.getAssignedAgent() != null) {
            notificationService.createTicketNotification(
                    ticket.getAssignedAgent(),
                    ticket,
                    "New customer message on ticket #" + ticket.getTicketId());
        }

        systemLogService.logAiResponse(ticket, "AI processed a customer chat message");
        return chatResponse(ticket, result);
    }

    /**
     * Creates a ticket for a customer identified by email.
     *
     * @param request       ticket creation request
     * @param customerEmail authenticated customer's email address
     * @return created ticket
     */
    @Transactional
    public Ticket createCustomerTicket(CreateTicketRequest request, String customerEmail) {
        User customer = requireCustomerByEmail(customerEmail);

        if (request.getConversationId() == null) {
            if (request.getMessage() != null && !request.getMessage().isBlank()) {
                return createManualTicket(request, customer);
            }
            throw new BadRequestException("conversationId is required");
        }

        Conversation conversation = getConversation(request.getConversationId());
        assertCustomerOwnsConversation(conversation, customer);

        Ticket ticket = new Ticket();
        ticket.setConversation(conversation);
        ticket.setCustomer(customer);
        ticket.setTitle(defaultTitle(request.getTitle(), "Customer support ticket"));
        ticket.setCategory(defaultCategory(request.getCategory()));
        ticket.setPriority(defaultPriority(request.getPriority()));
        ticket.setStatus(TicketStatus.OPEN);

        Ticket savedTicket = ticketRepository.save(ticket);
        systemLogService.logTicketCreated(savedTicket);
        return savedTicket;
    }

    /**
     * Creates a manual support ticket for the currently authenticated customer.
     *
     * @param request ticket creation request
     * @return complete details of the created ticket
     */
    @Transactional
    public TicketDetailsResponse createManualTicket(CreateTicketRequest request) {
        User customer = requireCustomer(userService.currentUser());
        Ticket ticket = createManualTicket(request, customer);
        return details(ticket);
    }

    /**
     * Creates a manual customer-support ticket and routes it to human support.
     *
     * <p>
     * The customer message is analysed, persisted in the conversation,
     * and used to populate ticket classification information. The ticket
     * is immediately escalated and an available support agent is assigned
     * when one exists.
     * </p>
     *
     * @param request  ticket creation request
     * @param customer customer creating the ticket
     * @return created ticket
     */
    private Ticket createManualTicket(CreateTicketRequest request, User customer) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new BadRequestException("message is required for a manual ticket");
        }

        Conversation conversation;
        if (request.getConversationId() == null) {
            conversation = createConversation(customer);
        } else {
            conversation = getConversation(request.getConversationId());
            assertCustomerOwnsConversation(conversation, customer);
        }

        AiResult result = aiService.analyse(request.getMessage());

        if (!result.inScope()) {
            throw new BadRequestException(result.reply());
        }

        messageService.createMessage(
                conversation,
                customer,
                SenderType.CLIENT,
                request.getMessage(),
                result.sentiment(),
                result.sentimentScore());

        Ticket ticket = new Ticket();
        ticket.setConversation(conversation);
        ticket.setCustomer(customer);
        ticket.setTitle(defaultTitle(request.getTitle(), titleFrom(request.getMessage())));
        ticket.setCategory(request.getCategory() == null ? result.category() : request.getCategory());
        ticket.setPriority(request.getPriority() == null ? result.priority() : request.getPriority());
        ticket.setStatus(TicketStatus.ESCALATED);
        ticket.setSentiment(result.sentiment());
        ticket.setSentimentScore(result.sentimentScore());
        ticket.setAiConfidenceScore(result.confidence());
        ticket.setEscalatedAt(LocalDateTime.now());
        ticket.setEscalationReason("Customer created a manual support ticket");
        ticket.setAssignedAgent(findFirstAvailableAgent());

        ticket = ticketRepository.save(ticket);

        String acknowledgement = "Your support ticket has been created and routed to a human agent. Reference: #"
                + ticket.getTicketId();

        messageService.createMessage(
                conversation,
                null,
                SenderType.SYSTEM,
                acknowledgement,
                Sentiment.NEUTRAL,
                0.0);

        ticket.setFirstResponseAt(LocalDateTime.now());
        conversation.setStatus(ConversationStatus.ESCALATED);
        ticket = ticketRepository.save(ticket);
        conversationRepository.save(conversation);

        if (ticket.getAssignedAgent() != null) {
            notificationService.createTicketNotification(
                    ticket.getAssignedAgent(),
                    ticket,
                    "A new support ticket has been assigned to you");
        }

        systemLogService.logTicketCreated(ticket);
        return ticket;
    }

    /**
     * Retrieves ticket history for the specified customer.
     *
     * @param customerEmail customer's email address
     * @return customer tickets ordered according to the repository query
     */
    @Transactional(readOnly = true)
    public List<Ticket> getCustomerTicketHistory(String customerEmail) {
        User customer = requireCustomerByEmail(customerEmail);
        return ticketRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
    }

    /**
     * Retrieves ticket summaries belonging to the currently authenticated customer.
     *
     * @return list of customer ticket summaries
     */
    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getMyTicketSummaries() {
        User customer = requireCustomer(userService.currentUser());
        return ticketRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId())
                .stream()
                .map(TicketSummaryResponse::from)
                .toList();
    }

    /**
     * Counts all tickets belonging to the currently authenticated customer.
     *
     * @return total customer ticket count
     */
    @Transactional(readOnly = true)
    public long getMyTotalCount() {
        User customer = requireCustomer(userService.currentUser());
        return ticketRepository.countByCustomerUserId(customer.getUserId());
    }

    /**
     * Counts active tickets belonging to the currently authenticated customer.
     *
     * @return number of open, escalated, in-progress, or on-hold tickets
     */
    @Transactional(readOnly = true)
    public long getMyOpenCount() {
        User customer = requireCustomer(userService.currentUser());
        return ticketRepository.countByCustomerUserIdAndStatusIn(
                customer.getUserId(),
                Set.of(
                        TicketStatus.OPEN,
                        TicketStatus.ESCALATED,
                        TicketStatus.IN_PROGRESS,
                        TicketStatus.ON_HOLD));
    }

    /**
     * Retrieves ticket details after verifying customer ownership.
     *
     * @param ticketId      ticket identifier
     * @param customerEmail customer's email address
     * @return complete ticket details
     */
    @Transactional(readOnly = true)
    public TicketDetailsResponse getCustomerTicketDetails(Long ticketId, String customerEmail) {
        User customer = requireCustomerByEmail(customerEmail);
        Ticket ticket = getTicketById(ticketId);
        assertCustomerOwnsTicket(ticket, customer);
        return details(ticket);
    }

    /**
     * Retrieves complete ticket details after validating access for the
     * currently authenticated user.
     *
     * @param ticketId ticket identifier
     * @return complete ticket details
     */
    @Transactional(readOnly = true)
    public TicketDetailsResponse getAccessibleTicketDetails(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        assertCanView(ticket, userService.currentUser());
        return details(ticket);
    }

    /**
     * Validates and persists a ticket.
     *
     * @param ticket ticket to save
     * @return persisted ticket
     */
    @Transactional
    public Ticket saveTicket(Ticket ticket) {
        if (ticket.getConversation() == null || ticket.getConversation().getConversationId() == null) {
            throw new BadRequestException("Ticket conversation is required");
        }
        if (ticket.getCustomer() == null || ticket.getCustomer().getUserId() == null) {
            throw new BadRequestException("Ticket customer is required");
        }

        ticket.setConversation(getConversation(ticket.getConversation().getConversationId()));
        ticket.setCustomer(userService.getUserById(ticket.getCustomer().getUserId()));

        if (ticket.getAssignedAgent() != null && ticket.getAssignedAgent().getUserId() != null) {
            ticket.setAssignedAgent(userService.getUserById(ticket.getAssignedAgent().getUserId()));
        }

        if (ticket.getCategory() == null) {
            ticket.setCategory(TicketCategory.GENERAL_INQUIRY);
        }
        if (ticket.getPriority() == null) {
            ticket.setPriority(TicketPriority.MEDIUM);
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus(TicketStatus.OPEN);
        }

        return ticketRepository.save(ticket);
    }

    /**
     * Retrieves all tickets.
     *
     * @return all persisted tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    /**
     * Retrieves a ticket using its identifier.
     *
     * @param ticketId ticket identifier
     * @return matching ticket
     * @throws ResourceNotFoundException if the ticket does not exist
     */
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with ID: " + ticketId));
    }

    /**
     * Deletes the specified ticket after confirming that it exists.
     *
     * @param ticketId ticket identifier
     */
    @Transactional
    public void deleteTicket(Long ticketId) {
        getTicketById(ticketId);
        ticketRepository.deleteById(ticketId);
    }

    /**
     * Escalates a ticket for human support and attempts automatic
     * assignment to an active support agent.
     *
     * @param ticketId ticket identifier
     * @return escalated ticket
     */
    @Transactional
    public Ticket escalateTicket(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ForbiddenOperationException("Closed tickets cannot be escalated");
        }

        ticket.setStatus(TicketStatus.ESCALATED);
        if (ticket.getEscalatedAt() == null) {
            ticket.setEscalatedAt(LocalDateTime.now());
        }
        if (ticket.getEscalationReason() == null || ticket.getEscalationReason().isBlank()) {
            ticket.setEscalationReason("Ticket escalated for human assistance");
        }
        if (ticket.getAssignedAgent() == null) {
            ticket.setAssignedAgent(findFirstAvailableAgent());
        }

        ticket.getConversation().setStatus(ConversationStatus.ESCALATED);
        conversationRepository.save(ticket.getConversation());
        Ticket savedTicket = ticketRepository.save(ticket);
        systemLogService.logTicketEscalated(savedTicket);

        notifyActiveAgents(savedTicket, "New escalated ticket #" + savedTicket.getTicketId());
        return savedTicket;
    }

    /**
     * Retrieves tickets currently in the escalated state.
     *
     * @return escalated tickets
     */
    @Transactional(readOnly = true)
    public List<Ticket> getEscalatedTickets() {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.ESCALATED);
    }

    /**
     * Retrieves tickets accessible to the currently authenticated staff user.
     *
     * @return ticket summaries accessible to the current staff member
     */
    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getStaffTickets() {
        User current = userService.currentUser();

        if (current.getRole() == UserRole.ADMIN) {
            return ticketRepository.findAll().stream()
                    .map(TicketSummaryResponse::from)
                    .toList();
        }

        if (current.getRole() != UserRole.AGENT) {
            throw new ForbiddenOperationException("Staff access is required");
        }

        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.isEscalated()
                        && (ticket.getAssignedAgent() == null
                                || ticket.getAssignedAgent().getUserId().equals(current.getUserId())))
                .map(TicketSummaryResponse::from)
                .toList();
    }

    /**
     * Assigns a ticket to the support agent identified by email.
     *
     * @param ticketId   ticket identifier
     * @param agentEmail support agent email
     * @return assigned ticket
     */
    @Transactional
    public Ticket assignTicketToAgent(Long ticketId, String agentEmail) {
        User agent = requireSupportAgentByEmail(agentEmail);
        return assignTicket(ticketId, agent, agent);
    }

    /**
     * Assigns a ticket to a specific active support agent.
     *
     * @param ticketId ticket identifier
     * @param agentId  support agent identifier
     * @return updated ticket details
     */
    @Transactional
    public TicketDetailsResponse assignTicketToSpecificAgent(Long ticketId, Long agentId) {
        User current = userService.currentUser();
        User agent = userService.getUserById(agentId);

        if (agent.getRole() != UserRole.AGENT || agent.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("The selected user is not an active support agent");
        }

        if (current.getRole() == UserRole.AGENT
                && !current.getUserId().equals(agent.getUserId())) {
            throw new ForbiddenOperationException("Support agents may only assign tickets to themselves");
        }

        if (current.getRole() == UserRole.CLIENT) {
            throw new ForbiddenOperationException("Staff access is required");
        }

        return details(assignTicket(ticketId, agent, current));
    }

    /**
     * Assigns a ticket to the selected support agent and records the assignment.
     *
     * @param ticketId ticket identifier
     * @param agent    agent receiving the ticket
     * @param actor    user performing the assignment
     * @return assigned ticket
     */
    private Ticket assignTicket(Long ticketId, User agent, User actor) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setAssignedAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        if (ticket.getEscalatedAt() == null) {
            ticket.setEscalatedAt(LocalDateTime.now());
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        notificationService.createTicketNotification(
                savedTicket.getCustomer(),
                savedTicket,
                "A support agent has been assigned to your ticket.");
        systemLogService.logEvent(
                "TICKET_ASSIGNED",
                "Ticket assigned to " + agent.getName(),
                actor,
                savedTicket);
        return savedTicket;
    }

    /**
     * Updates a ticket assigned to the authenticated support agent.
     *
     * @param ticketId   ticket identifier
     * @param request    requested status and resolution changes
     * @param agentEmail authenticated support agent email
     * @return updated ticket
     */
    @Transactional
    public Ticket updateAssignedTicket(
            Long ticketId,
            AgentTicketUpdateRequest request,
            String agentEmail) {

        User agent = requireSupportAgentByEmail(agentEmail);
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getAssignedAgent() == null
                || !ticket.getAssignedAgent().getUserId().equals(agent.getUserId())) {
            throw new ForbiddenOperationException(
                    "Ticket is not assigned to the authenticated support agent");
        }

        applyStaffStatus(ticket, request.getStatus(), request.getResolutionNotes(), agent);
        return ticketRepository.save(ticket);
    }

    /**
     * Updates ticket status using the currently authenticated staff user.
     *
     * @param ticketId ticket identifier
     * @param request  requested ticket status information
     * @return updated ticket details
     */
    @Transactional
    public TicketDetailsResponse updateStatusByCurrentStaff(
            Long ticketId,
            TicketStatusRequest request) {

        User current = userService.currentUser();
        if (current.getRole() == UserRole.CLIENT) {
            throw new ForbiddenOperationException("Only support staff can update ticket status");
        }

        Ticket ticket = getTicketById(ticketId);
        assertCanView(ticket, current);
        applyStaffStatus(ticket, request.getStatus(), request.getResolutionNotes(), current);
        return details(ticketRepository.save(ticket));
    }

    /**
     * Applies a staff-requested status change and updates the appropriate
     * ticket and conversation timestamps.
     *
     * @param ticket          ticket being updated
     * @param status          requested ticket status
     * @param resolutionNotes optional resolution notes
     * @param actor           staff user performing the update
     */
    private void applyStaffStatus(
            Ticket ticket,
            TicketStatus status,
            String resolutionNotes,
            User actor) {

        if (status == null) {
            throw new BadRequestException("Ticket status is required");
        }

        if (resolutionNotes != null) {
            ticket.setResolutionNotes(resolutionNotes.trim());
        }

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED || status == TicketStatus.RESOLVED_BY_AI) {
            ticket.setResolvedAt(LocalDateTime.now());
            ticket.getConversation().setStatus(ConversationStatus.COMPLETED);
            ticket.getConversation().setEndedAt(LocalDateTime.now());
            notificationService.createTicketNotification(
                    ticket.getCustomer(),
                    ticket,
                    "Ticket #" + ticket.getTicketId() + " has been resolved");
        } else if (status == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }
            ticket.getConversation().setStatus(ConversationStatus.COMPLETED);
            ticket.getConversation().setEndedAt(LocalDateTime.now());
            notificationService.createTicketNotification(
                    ticket.getCustomer(),
                    ticket,
                    "Ticket #" + ticket.getTicketId() + " has been closed");
        }

        conversationRepository.save(ticket.getConversation());
        systemLogService.logEvent(
                "TICKET_STATUS_UPDATED",
                "Status changed to " + status,
                actor,
                ticket);
    }

    /**
     * Saves a response from the authenticated support agent.
     *
     * <p>
     * The response is added to the ticket conversation and the first
     * human-response timestamp is recorded when required.
     * </p>
     *
     * @param ticketId   ticket identifier
     * @param request    support-agent message request
     * @param agentEmail authenticated support agent email
     * @return persisted agent message
     */
    @Transactional
    public Message sendAgentResponse(
            Long ticketId,
            AgentMessageRequest request,
            String agentEmail) {

        User agent = requireSupportAgentByEmail(agentEmail);
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getAssignedAgent() == null
                || !ticket.getAssignedAgent().getUserId().equals(agent.getUserId())) {
            throw new ForbiddenOperationException(
                    "Ticket is not assigned to the authenticated support agent");
        }

        Message message = messageService.createMessage(
                ticket.getConversation(),
                agent,
                SenderType.AGENT,
                request.getContent().trim(),
                Sentiment.NEUTRAL,
                0.0);

        if (ticket.getFirstResponseAt() == null) {
            ticket.setFirstResponseAt(LocalDateTime.now());
        }
        if (ticket.getStatus() == TicketStatus.ESCALATED || ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticketRepository.save(ticket);
        notificationService.createTicketNotification(
                ticket.getCustomer(),
                ticket,
                "A support agent replied to ticket #" + ticket.getTicketId());
        systemLogService.logEvent("AGENT_MESSAGE_SENT", "Support agent replied to ticket", agent, ticket);
        return message;
    }

    /**
     * Adds a manual message to a ticket after validating access.
     *
     * <p>
     * The sender type is derived from the authenticated user's role.
     * Agent messages can update assignment and response information,
     * while customer follow-up messages can reopen and escalate previously
     * resolved tickets.
     * </p>
     *
     * @param ticketId ticket identifier
     * @param request  message request
     * @return updated ticket details
     */
    @Transactional
    public TicketDetailsResponse addManualMessage(Long ticketId, TicketMessageRequest request) {
        String text = request.text();
        if (text == null || text.isBlank()) {
            throw new BadRequestException("message is required");
        }

        Ticket ticket = getTicketById(ticketId);
        User current = userService.currentUser();
        assertCanView(ticket, current);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("This ticket is closed");
        }

        SenderType senderType = current.getRole() == UserRole.CLIENT
                ? SenderType.CLIENT
                : SenderType.AGENT;

        messageService.createMessage(
                ticket.getConversation(),
                current,
                senderType,
                text.trim(),
                Sentiment.NEUTRAL,
                0.0);

        if (senderType == SenderType.AGENT) {
            if (ticket.getAssignedAgent() == null) {
                ticket.setAssignedAgent(current);
            }
            if (ticket.getStatus() == TicketStatus.ESCALATED || ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }
            if (ticket.getFirstResponseAt() == null) {
                ticket.setFirstResponseAt(LocalDateTime.now());
            }
            notificationService.createTicketNotification(
                    ticket.getCustomer(),
                    ticket,
                    "A support agent replied to ticket #" + ticket.getTicketId());
        } else {
            if (ticket.getStatus() == TicketStatus.RESOLVED_BY_AI || ticket.getStatus() == TicketStatus.RESOLVED) {
                ticket.setStatus(TicketStatus.ESCALATED);
                ticket.setEscalatedAt(LocalDateTime.now());
                ticket.setResolvedAt(null);
                ticket.setEscalationReason("Customer added a follow-up message after resolution");
                if (ticket.getAssignedAgent() == null) {
                    ticket.setAssignedAgent(findFirstAvailableAgent());
                }
            }
            if (ticket.getAssignedAgent() != null) {
                notificationService.createTicketNotification(
                        ticket.getAssignedAgent(),
                        ticket,
                        "Customer replied to ticket #" + ticket.getTicketId());
            }
        }

        ticketRepository.save(ticket);
        systemLogService.logEvent(
                "TICKET_MESSAGE_ADDED",
                senderType + " message added",
                current,
                ticket);
        return details(ticket);
    }

    /**
     * Creates a ticket using the classification and analysis returned by AI.
     *
     * @param conversation associated conversation
     * @param customer     customer who created the conversation
     * @param title        ticket title
     * @param result       AI analysis result
     * @return persisted AI-created ticket
     */
    private Ticket createAiTicket(
            Conversation conversation,
            User customer,
            String title,
            AiResult result) {

        Ticket ticket = new Ticket();
        ticket.setConversation(conversation);
        ticket.setCustomer(customer);
        ticket.setTitle(title);
        applyAiResult(ticket, result);
        return ticketRepository.save(ticket);
    }

    /**
     * Applies the latest AI analysis values to a ticket.
     *
     * @param ticket ticket being updated
     * @param result AI analysis result
     */
    private void applyAiResult(Ticket ticket, AiResult result) {
        ticket.setCategory(result.category());
        ticket.setPriority(result.priority());
        ticket.setSentiment(result.sentiment());
        ticket.setSentimentScore(result.sentimentScore());
        ticket.setAiConfidenceScore(result.confidence());

        if (result.escalated()) {
            ticket.setStatus(TicketStatus.ESCALATED);
            if (ticket.getEscalatedAt() == null) {
                ticket.setEscalatedAt(LocalDateTime.now());
            }
            ticket.setEscalationReason(String.join("; ", result.escalationReasons()));
            if (ticket.getAssignedAgent() == null) {
                ticket.setAssignedAgent(findFirstAvailableAgent());
            }
        } else if (!ticket.isEscalated()) {
            ticket.setStatus(TicketStatus.RESOLVED_BY_AI);
            ticket.setResolvedAt(LocalDateTime.now());
        }
    }

    /**
     * Performs notification and logging operations after AI processing.
     *
     * @param ticket processed ticket
     * @param result AI analysis result
     */
    private void afterAiProcessing(Ticket ticket, AiResult result) {
        if (ticket.isEscalated() && ticket.getAssignedAgent() != null) {
            notificationService.createTicketNotification(
                    ticket.getAssignedAgent(),
                    ticket,
                    "AI escalated ticket #" + ticket.getTicketId());
        }

        systemLogService.logEvent(
                ticket.isEscalated() ? "TICKET_ESCALATED" : "TICKET_RESOLVED_BY_AI",
                result.knowledgeBaseMatch()
                        ? "Knowledge base response used by AI"
                        : "AI response generated",
                ticket.getCustomer(),
                ticket);
    }

    /**
     * Converts a ticket and AI result into a chat response.
     *
     * @param ticket ticket associated with the chat
     * @param result AI analysis result
     * @return constructed chat response
     */
    private ChatResponse chatResponse(Ticket ticket, AiResult result) {
        AiAnalysisResponse analysis = new AiAnalysisResponse(
                result.category(),
                result.priority(),
                result.sentiment(),
                result.sentimentScore(),
                result.confidence(),
                result.escalated(),
                result.escalationReasons(),
                result.knowledgeBaseMatch());

        return new ChatResponse(
                result.reply(),
                TicketSummaryResponse.from(ticket),
                analysis);
    }

    /**
     * Builds complete ticket details including conversation messages.
     *
     * @param ticket ticket to convert
     * @return complete ticket details
     */
    private TicketDetailsResponse details(Ticket ticket) {
        List<Message> messages = messageService.getConversationMessages(
                ticket.getConversation().getConversationId());
        return TicketDetailsResponse.from(ticket, messages);
    }

    /**
     * Updates conversation status based on the current ticket state.
     *
     * @param conversation conversation associated with the ticket
     * @param ticket       ticket whose state determines the conversation status
     */
    private void finishConversationFromTicket(Conversation conversation, Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.RESOLVED_BY_AI
                || ticket.getStatus() == TicketStatus.RESOLVED
                || ticket.getStatus() == TicketStatus.CLOSED) {
            conversation.setStatus(ConversationStatus.COMPLETED);
            conversation.setEndedAt(LocalDateTime.now());
        } else if (ticket.isEscalated()) {
            conversation.setStatus(ConversationStatus.ESCALATED);
        }
    }

    /**
     * Creates a new active conversation for a customer.
     *
     * @param customer customer associated with the conversation
     * @return persisted conversation
     */
    private Conversation createConversation(User customer) {
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    /**
     * Retrieves a conversation by identifier.
     *
     * @param conversationId conversation identifier
     * @return matching conversation
     * @throws ResourceNotFoundException if the conversation does not exist
     */
    private Conversation getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with ID: " + conversationId));
    }

    /**
     * Retrieves and validates a customer using an email address.
     *
     * @param email customer email
     * @return validated customer
     */
    private User requireCustomerByEmail(String email) {
        User customer = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with email: " + email));
        return requireCustomer(customer);
    }

    /**
     * Verifies that the supplied user has the client role.
     *
     * @param user user to validate
     * @return validated customer
     */
    private User requireCustomer(User user) {
        if (user.getRole() != UserRole.CLIENT) {
            throw new ForbiddenOperationException("Customer access is required");
        }
        return user;
    }

    /**
     * Retrieves and validates an active support agent by email.
     *
     * @param email support agent email
     * @return active support agent
     */
    private User requireSupportAgentByEmail(String email) {
        User agent = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Support agent not found with email: " + email));

        if (agent.getRole() != UserRole.AGENT || agent.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenOperationException("Active support-agent access is required");
        }
        return agent;
    }

    /**
     * Verifies that a conversation belongs to the specified customer.
     *
     * @param conversation conversation to validate
     * @param customer     authenticated customer
     */
    private void assertCustomerOwnsConversation(Conversation conversation, User customer) {
        if (!conversation.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new ForbiddenOperationException(
                    "Conversation does not belong to the authenticated customer");
        }
    }

    /**
     * Verifies that a ticket belongs to the specified customer.
     *
     * @param ticket   ticket to validate
     * @param customer authenticated customer
     */
    private void assertCustomerOwnsTicket(Ticket ticket, User customer) {
        if (!ticket.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new ForbiddenOperationException(
                    "Ticket does not belong to the authenticated customer");
        }
    }

    /**
     * Verifies whether the currently authenticated user may access a ticket.
     *
     * <p>
     * Administrators may access all tickets, clients may access their own
     * tickets, and agents may access unassigned tickets or tickets assigned
     * to themselves.
     * </p>
     *
     * @param ticket  ticket being accessed
     * @param current authenticated user
     */
    private void assertCanView(Ticket ticket, User current) {
        if (current.getRole() == UserRole.ADMIN) {
            return;
        }
        if (current.getRole() == UserRole.CLIENT
                && ticket.getCustomer().getUserId().equals(current.getUserId())) {
            return;
        }
        if (current.getRole() == UserRole.AGENT
                && (ticket.getAssignedAgent() == null
                        || ticket.getAssignedAgent().getUserId().equals(current.getUserId()))) {
            return;
        }
        throw new ForbiddenOperationException("You do not have access to this ticket");
    }

    /**
     * Finds the first active support agent available for automatic assignment.
     *
     * @return active support agent, or {@code null} when none is available
     */
    private User findFirstAvailableAgent() {
        return userRepository.findFirstByRoleAndStatusOrderByUserIdAsc(
                UserRole.AGENT,
                UserStatus.ACTIVE)
                .orElse(null);
    }

    /**
     * Sends the supplied ticket notification to all active support agents.
     *
     * @param ticket  ticket associated with the notification
     * @param message notification message
     */
    private void notifyActiveAgents(Ticket ticket, String message) {
        for (User agent : userRepository.findByRoleAndStatusOrderByNameAsc(
                UserRole.AGENT,
                UserStatus.ACTIVE)) {
            notificationService.createTicketNotification(agent, ticket, message);
        }
    }

    /**
     * Returns a cleaned ticket title or the supplied fallback when no
     * title is provided.
     *
     * @param title    requested title
     * @param fallback fallback title
     * @return normalized ticket title
     */
    private String defaultTitle(String title, String fallback) {
        if (title == null || title.isBlank()) {
            return fallback;
        }
        String clean = title.trim();
        return clean.length() <= 180 ? clean : clean.substring(0, 180);
    }

    /**
     * Generates a ticket title from a customer message.
     *
     * @param message customer message
     * @return generated ticket title
     */
    private String titleFrom(String message) {
        String clean = message.trim().replaceAll("\\s+", " ");
        return clean.length() <= 70 ? clean : clean.substring(0, 67) + "...";
    }

    /**
     * Returns the supplied category or the default general-inquiry category.
     *
     * @param category requested ticket category
     * @return supplied or default category
     */
    private TicketCategory defaultCategory(TicketCategory category) {
        return category == null ? TicketCategory.GENERAL_INQUIRY : category;
    }

    /**
     * Returns the supplied priority or the default medium priority.
     *
     * @param priority requested ticket priority
     * @return supplied or default priority
     */
    private TicketPriority defaultPriority(TicketPriority priority) {
        return priority == null ? TicketPriority.MEDIUM : priority;
    }

    /**
     * Returns the higher of the current and newly calculated ticket priorities.
     *
     * @param current current ticket priority
     * @param next    newly calculated ticket priority
     * @return higher priority
     */
    private TicketPriority higherPriority(TicketPriority current, TicketPriority next) {
        if (current == null) {
            return next;
        }
        return next.ordinal() > current.ordinal() ? next : current;
    }
}