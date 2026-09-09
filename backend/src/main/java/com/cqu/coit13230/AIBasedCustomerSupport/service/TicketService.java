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
 * Handles ticket, chat, assignment and escalation workflows.
 * Most ticket state changes, access checks and customer/agent message flows are handled here.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final MessageService messageService;
    private final NotificationService notificationService;
    private final SystemLogService systemLogService;
    private final AiService aiService;

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

    /*
     * Starts a new customer conversation and stores the first message before running AI analysis.
     * A ticket is then created from the analysis and linked back to the conversation.
     */
    @Transactional
    public ChatResponse startChat(String customerMessage) {
        User customer = requireCustomer(userService.currentUser());

        AiResult result = aiService.analyse(customerMessage);

        // Reject unrelated requests before creating a support conversation or ticket.
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

    /*
     * Continues an existing chat by saving the customer message and running AI analysis again.
     * The latest AI values are copied onto the same ticket so its state stays up to date.
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

    @Transactional
    public TicketDetailsResponse createManualTicket(CreateTicketRequest request) {
        User customer = requireCustomer(userService.currentUser());
        Ticket ticket = createManualTicket(request, customer);
        return details(ticket);
    }

    // Creates a customer ticket without an AI chat flow and sends it directly to human support.
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

    @Transactional(readOnly = true)
    public List<Ticket> getCustomerTicketHistory(String customerEmail) {
        User customer = requireCustomerByEmail(customerEmail);
        return ticketRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId());
    }

    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getMyTicketSummaries() {
        User customer = requireCustomer(userService.currentUser());
        return ticketRepository.findByCustomerUserIdOrderByCreatedAtDesc(customer.getUserId())
                .stream()
                .map(TicketSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getMyTotalCount() {
        User customer = requireCustomer(userService.currentUser());
        return ticketRepository.countByCustomerUserId(customer.getUserId());
    }

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

    @Transactional(readOnly = true)
    public TicketDetailsResponse getCustomerTicketDetails(Long ticketId, String customerEmail) {
        User customer = requireCustomerByEmail(customerEmail);
        Ticket ticket = getTicketById(ticketId);
        assertCustomerOwnsTicket(ticket, customer);
        return details(ticket);
    }

    // Checks the current role and ticket ownership before returning full ticket details and messages.
    @Transactional(readOnly = true)
    public TicketDetailsResponse getAccessibleTicketDetails(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        assertCanView(ticket, userService.currentUser());
        return details(ticket);
    }

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

    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with ID: " + ticketId));
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        getTicketById(ticketId);
        ticketRepository.deleteById(ticketId);
    }

    /*
     * Marks the ticket for human support and tries to assign an active agent automatically.
     * The ticket can remain unassigned if there is no active agent available at the time.
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

    @Transactional(readOnly = true)
    public List<Ticket> getEscalatedTickets() {
        return ticketRepository.findByStatusOrderByCreatedAtAsc(TicketStatus.ESCALATED);
    }

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

    @Transactional
    public Ticket assignTicketToAgent(Long ticketId, String agentEmail) {
        User agent = requireSupportAgentByEmail(agentEmail);
        return assignTicket(ticketId, agent, agent);
    }

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

    // Confirms the selected user is an active agent before linking the agent to the ticket.
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

    // Updates resolved or closed timestamps when staff change the ticket status.
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

    // Saves the agent reply to the conversation and records the first human response time when needed.
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

    /*
     * Saves a new ticket message only after checking customer ownership or staff access.
     * The sender type is set from the logged-in user's role before the message is stored.
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

    // Creates the ticket using the classification, priority, sentiment and confidence returned by AI.
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

    // Refreshes the existing ticket with the latest AI classification and escalation result.
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

    /*
     * Records the AI decision in the system log for traceability.
     * When human review is required, agent notifications are created as part of the same workflow.
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

    private TicketDetailsResponse details(Ticket ticket) {
        List<Message> messages = messageService.getConversationMessages(
                ticket.getConversation().getConversationId());
        return TicketDetailsResponse.from(ticket, messages);
    }

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

    private Conversation createConversation(User customer) {
        Conversation conversation = new Conversation();
        conversation.setCustomer(customer);
        conversation.setStatus(ConversationStatus.ACTIVE);
        return conversationRepository.save(conversation);
    }

    private Conversation getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found with ID: " + conversationId));
    }

    private User requireCustomerByEmail(String email) {
        User customer = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with email: " + email));
        return requireCustomer(customer);
    }

    private User requireCustomer(User user) {
        if (user.getRole() != UserRole.CLIENT) {
            throw new ForbiddenOperationException("Customer access is required");
        }
        return user;
    }

    private User requireSupportAgentByEmail(String email) {
        User agent = userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Support agent not found with email: " + email));

        if (agent.getRole() != UserRole.AGENT || agent.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenOperationException("Active support-agent access is required");
        }
        return agent;
    }

    private void assertCustomerOwnsConversation(Conversation conversation, User customer) {
        if (!conversation.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new ForbiddenOperationException(
                    "Conversation does not belong to the authenticated customer");
        }
    }

    private void assertCustomerOwnsTicket(Ticket ticket, User customer) {
        if (!ticket.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new ForbiddenOperationException(
                    "Ticket does not belong to the authenticated customer");
        }
    }

    // Allows customers to access only their own tickets while staff can access tickets for support work.
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

    // Uses the first active agent returned by the user service for automatic assignment.
    private User findFirstAvailableAgent() {
        return userRepository.findFirstByRoleAndStatusOrderByUserIdAsc(
                        UserRole.AGENT,
                        UserStatus.ACTIVE)
                .orElse(null);
    }

    private void notifyActiveAgents(Ticket ticket, String message) {
        for (User agent : userRepository.findByRoleAndStatusOrderByNameAsc(
                UserRole.AGENT,
                UserStatus.ACTIVE)) {
            notificationService.createTicketNotification(agent, ticket, message);
        }
    }

    private String defaultTitle(String title, String fallback) {
        if (title == null || title.isBlank()) {
            return fallback;
        }
        String clean = title.trim();
        return clean.length() <= 180 ? clean : clean.substring(0, 180);
    }

    private String titleFrom(String message) {
        String clean = message.trim().replaceAll("\\s+", " ");
        return clean.length() <= 70 ? clean : clean.substring(0, 67) + "...";
    }

    private TicketCategory defaultCategory(TicketCategory category) {
        return category == null ? TicketCategory.GENERAL_INQUIRY : category;
    }

    private TicketPriority defaultPriority(TicketPriority priority) {
        return priority == null ? TicketPriority.MEDIUM : priority;
    }

    private TicketPriority higherPriority(TicketPriority current, TicketPriority next) {
        if (current == null) {
            return next;
        }
        return next.ordinal() > current.ordinal() ? next : current;
    }
}
