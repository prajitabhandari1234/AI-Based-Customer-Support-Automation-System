package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import com.cqu.coit13230.AIBasedCustomerSupport.ai.AiProcessingService;
import com.cqu.coit13230.AIBasedCustomerSupport.ai.AiResult;
import com.cqu.coit13230.AIBasedCustomerSupport.ai.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.ai.SentimentService;
import com.cqu.coit13230.AIBasedCustomerSupport.chat.ChatResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.chat.AiAnalysisView;
import com.cqu.coit13230.AIBasedCustomerSupport.common.BadRequestException;
import com.cqu.coit13230.AIBasedCustomerSupport.common.ForbiddenException;
import com.cqu.coit13230.AIBasedCustomerSupport.common.NotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.log.SystemLogService;
import com.cqu.coit13230.AIBasedCustomerSupport.message.*;
import com.cqu.coit13230.AIBasedCustomerSupport.notification.NotificationService;
import com.cqu.coit13230.AIBasedCustomerSupport.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final MessageRepository messages;
    private final UserRepository users;
    private final UserService userService;
    private final AiProcessingService ai;
    private final SentimentService sentimentService;
    private final NotificationService notifications;
    private final SystemLogService logs;

    public TicketService(TicketRepository tickets, MessageRepository messages, UserRepository users,
                         UserService userService, AiProcessingService ai, SentimentService sentimentService,
                         NotificationService notifications, SystemLogService logs) {
        this.tickets = tickets;
        this.messages = messages;
        this.users = users;
        this.userService = userService;
        this.ai = ai;
        this.sentimentService = sentimentService;
        this.notifications = notifications;
        this.logs = logs;
    }

    @Transactional
    public ChatResponse startChat(String customerMessage) {
        User customer = userService.currentUser();
        AiResult result = ai.process(customerMessage);
        Ticket ticket = createTicket(customer, titleFrom(customerMessage), result, result.escalated());
        addMessage(ticket, customer, SenderType.CLIENT, customerMessage, result.sentiment(), result.sentimentScore());
        addMessage(ticket, null, SenderType.AI, result.reply(), Sentiment.NEUTRAL, 0);
        ticket.setFirstResponseAt(Instant.now());
        ticket = tickets.save(ticket);
        afterCreated(ticket, customer, result);
        return new ChatResponse(result.reply(), TicketSummary.from(ticket), AiAnalysisView.from(result));
    }

    @Transactional
    public ChatResponse continueChat(Long ticketId, String customerMessage) {
        User customer = userService.currentUser();
        Ticket ticket = require(ticketId);
        if (!ticket.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenException("You cannot chat on another customer's ticket");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("This ticket is closed");
        }

        AiResult result = ai.process(customerMessage);
        addMessage(ticket, customer, SenderType.CLIENT, customerMessage, result.sentiment(), result.sentimentScore());

        ticket.setCategory(result.category());
        ticket.setPriority(higherPriority(ticket.getPriority(), result.priority()));
        ticket.setSentiment(result.sentiment());
        ticket.setSentimentScore(result.sentimentScore());
        ticket.setAiConfidenceScore(result.confidence());

        if (result.escalated()) {
            escalate(ticket, result);
        } else if (!ticket.isEscalated()) {
            ticket.setStatus(TicketStatus.RESOLVED_BY_AI);
            ticket.setResolvedAt(Instant.now());
        }

        addMessage(ticket, null, SenderType.AI, result.reply(), Sentiment.NEUTRAL, 0);
        if (ticket.getFirstResponseAt() == null) ticket.setFirstResponseAt(Instant.now());
        ticket = tickets.save(ticket);

        if (ticket.isEscalated() && ticket.getAssignedAgent() != null) {
            notifications.send(ticket.getAssignedAgent(), ticket,
                    "New customer message on ticket #" + ticket.getId());
        }
        logs.record("CHAT_MESSAGE_PROCESSED", "AI processed a customer message", customer, ticket);
        return new ChatResponse(result.reply(), TicketSummary.from(ticket), AiAnalysisView.from(result));
    }

    @Transactional
    public TicketDetail createManual(CreateTicketRequest request) {
        User customer = userService.currentUser();
        AiResult result = ai.process(request.message());
        Ticket ticket = createTicket(customer, request.title(), result, true);
        if (!result.escalated()) {
            ticket.setEscalated(true);
            ticket.setStatus(TicketStatus.ESCALATED);
            ticket.setEscalationReason("Customer created a manual support ticket");
            assignFirstAgent(ticket);
        }
        addMessage(ticket, customer, SenderType.CLIENT, request.message(), result.sentiment(), result.sentimentScore());
        String acknowledgement = "Your support ticket has been created and routed to a human agent. Reference: #" + ticket.getId();
        addMessage(ticket, null, SenderType.SYSTEM, acknowledgement, Sentiment.NEUTRAL, 0);
        ticket.setFirstResponseAt(Instant.now());
        ticket = tickets.save(ticket);
        if (ticket.getAssignedAgent() != null) {
            notifications.send(ticket.getAssignedAgent(), ticket, "A new support ticket has been assigned to you");
        }
        logs.record("TICKET_CREATED", "Manual support ticket created", customer, ticket);
        return detailUnchecked(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketSummary> myTickets() {
        User current = userService.currentUser();
        return tickets.findByCustomerOrderByUpdatedAtDesc(current).stream().map(TicketSummary::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TicketSummary> staffTickets() {
        User current = userService.currentUser();
        if (current.getRole() == Role.ADMIN) {
            return tickets.findAllByOrderByPriorityDescUpdatedAtDesc().stream().map(TicketSummary::from).toList();
        }
        if (current.getRole() != Role.AGENT) throw new ForbiddenException("Staff access is required");
        return tickets.findAllByOrderByPriorityDescUpdatedAtDesc().stream()
                .filter(ticket -> ticket.isEscalated()
                        && (ticket.getAssignedAgent() == null || ticket.getAssignedAgent().getId().equals(current.getId())))
                .map(TicketSummary::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketDetail detail(Long id) {
        Ticket ticket = require(id);
        assertCanView(ticket, userService.currentUser());
        return detailUnchecked(ticket);
    }

    @Transactional
    public TicketDetail addManualMessage(Long id, TicketMessageRequest request) {
        Ticket ticket = require(id);
        User current = userService.currentUser();
        assertCanView(ticket, current);
        if (ticket.getStatus() == TicketStatus.CLOSED) throw new BadRequestException("This ticket is closed");

        var sentiment = sentimentService.analyse(request.message());
        SenderType senderType = current.getRole() == Role.CLIENT ? SenderType.CLIENT : SenderType.AGENT;
        addMessage(ticket, current, senderType, request.message(), sentiment.sentiment(), sentiment.score());

        if (senderType == SenderType.AGENT) {
            if (ticket.getAssignedAgent() == null) ticket.setAssignedAgent(current);
            if (ticket.getStatus() == TicketStatus.ESCALATED || ticket.getStatus() == TicketStatus.NEW) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }
            if (ticket.getFirstResponseAt() == null) ticket.setFirstResponseAt(Instant.now());
            notifications.send(ticket.getCustomer(), ticket, "A support agent replied to ticket #" + ticket.getId());
        } else {
            if (sentiment.score() <= -0.4) {
                ticket.setPriority(higherPriority(ticket.getPriority(), TicketPriority.HIGH));
                ticket.setSentiment(sentiment.sentiment());
                ticket.setSentimentScore(sentiment.score());
            }
            if (ticket.getStatus() == TicketStatus.RESOLVED_BY_AI || ticket.getStatus() == TicketStatus.RESOLVED) {
                ticket.setEscalated(true);
                ticket.setStatus(TicketStatus.ESCALATED);
                ticket.setResolvedAt(null);
                ticket.setEscalationReason("Customer added a follow-up message after resolution");
                if (ticket.getAssignedAgent() == null) assignFirstAgent(ticket);
            }
            if (ticket.getAssignedAgent() != null) {
                notifications.send(ticket.getAssignedAgent(), ticket, "Customer replied to ticket #" + ticket.getId());
            }
        }
        tickets.save(ticket);
        logs.record("TICKET_MESSAGE_ADDED", senderType + " message added", current, ticket);
        return detailUnchecked(ticket);
    }

    @Transactional
    public TicketDetail updateStatus(Long id, TicketStatusRequest request) {
        Ticket ticket = require(id);
        User current = userService.currentUser();
        if (current.getRole() == Role.CLIENT) throw new ForbiddenException("Only support staff can update ticket status");
        assertCanView(ticket, current);

        ticket.setStatus(request.status());
        if (request.resolutionNotes() != null) ticket.setResolutionNotes(request.resolutionNotes().trim());
        if (request.status() == TicketStatus.RESOLVED || request.status() == TicketStatus.RESOLVED_BY_AI) {
            ticket.setResolvedAt(Instant.now());
            notifications.send(ticket.getCustomer(), ticket, "Ticket #" + ticket.getId() + " has been resolved");
        }
        if (request.status() == TicketStatus.CLOSED) {
            ticket.setClosedAt(Instant.now());
            if (ticket.getResolvedAt() == null) ticket.setResolvedAt(Instant.now());
            notifications.send(ticket.getCustomer(), ticket, "Ticket #" + ticket.getId() + " has been closed");
        }
        tickets.save(ticket);
        logs.record("TICKET_STATUS_UPDATED", "Status changed to " + request.status(), current, ticket);
        return detailUnchecked(ticket);
    }

    @Transactional
    public TicketDetail assign(Long id, Long agentId) {
        Ticket ticket = require(id);
        User current = userService.currentUser();
        User agent = userService.require(agentId);
        if (agent.getRole() != Role.AGENT || agent.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("The selected user is not an active support agent");
        }
        if (current.getRole() == Role.AGENT && !Objects.equals(current.getId(), agentId)) {
            throw new ForbiddenException("Agents may only assign tickets to themselves");
        }
        if (current.getRole() == Role.CLIENT) throw new ForbiddenException("Staff access is required");
        ticket.setAssignedAgent(agent);
        ticket.setEscalated(true);
        if (ticket.getStatus() == TicketStatus.NEW || ticket.getStatus() == TicketStatus.RESOLVED_BY_AI) {
            ticket.setStatus(TicketStatus.ESCALATED);
        }
        tickets.save(ticket);
        notifications.send(agent, ticket, "Ticket #" + ticket.getId() + " was assigned to you");
        logs.record("TICKET_ASSIGNED", "Ticket assigned to " + agent.getName(), current, ticket);
        return detailUnchecked(ticket);
    }

    @Transactional(readOnly = true)
    public long myOpenCount() {
        User current = userService.currentUser();
        return tickets.countByCustomerAndStatusIn(current,
                List.of(TicketStatus.NEW, TicketStatus.ESCALATED, TicketStatus.IN_PROGRESS, TicketStatus.ON_HOLD));
    }

    @Transactional(readOnly = true)
    public long myTotalCount() {
        return tickets.countByCustomer(userService.currentUser());
    }

    private Ticket createTicket(User customer, String title, AiResult result, boolean escalated) {
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setTitle(title.length() > 180 ? title.substring(0, 180) : title);
        ticket.setCategory(result.category());
        ticket.setPriority(result.priority());
        ticket.setSentiment(result.sentiment());
        ticket.setSentimentScore(result.sentimentScore());
        ticket.setAiConfidenceScore(result.confidence());
        ticket.setEscalated(escalated);
        ticket.setStatus(escalated ? TicketStatus.ESCALATED : TicketStatus.RESOLVED_BY_AI);
        if (escalated) {
            ticket.setEscalationReason(String.join("; ", result.escalationReasons()));
            assignFirstAgent(ticket);
        }
        ticket = tickets.save(ticket);
        if (!escalated) {
            ticket.setResolvedAt(Instant.now());
            ticket = tickets.save(ticket);
        }
        return ticket;
    }

    private void escalate(Ticket ticket, AiResult result) {
        ticket.setEscalated(true);
        ticket.setStatus(TicketStatus.ESCALATED);
        ticket.setEscalationReason(String.join("; ", result.escalationReasons()));
        if (ticket.getAssignedAgent() == null) assignFirstAgent(ticket);
    }

    private void assignFirstAgent(Ticket ticket) {
        users.findFirstByRoleAndStatusOrderByIdAsc(Role.AGENT, UserStatus.ACTIVE)
                .ifPresent(ticket::setAssignedAgent);
    }

    private void afterCreated(Ticket ticket, User customer, AiResult result) {
        if (ticket.isEscalated() && ticket.getAssignedAgent() != null) {
            notifications.send(ticket.getAssignedAgent(), ticket, "AI escalated ticket #" + ticket.getId());
        }
        logs.record(ticket.isEscalated() ? "TICKET_ESCALATED" : "TICKET_RESOLVED_BY_AI",
                result.knowledgeBaseMatch() ? "Knowledge base response used" : "AI/rule response generated",
                customer, ticket);
    }

    private Message addMessage(Ticket ticket, User sender, SenderType type, String content,
                               Sentiment sentiment, double sentimentScore) {
        Message message = new Message();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setSenderType(type);
        message.setContent(content);
        message.setSentiment(sentiment);
        message.setSentimentScore(sentimentScore);
        return messages.save(message);
    }

    private Ticket require(Long id) {
        return tickets.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    private void assertCanView(Ticket ticket, User current) {
        if (current.getRole() == Role.ADMIN) return;
        if (current.getRole() == Role.CLIENT && ticket.getCustomer().getId().equals(current.getId())) return;
        if (current.getRole() == Role.AGENT && (ticket.getAssignedAgent() == null
                || ticket.getAssignedAgent().getId().equals(current.getId()))) return;
        throw new ForbiddenException("You do not have access to this ticket");
    }

    private TicketDetail detailUnchecked(Ticket ticket) {
        return new TicketDetail(
                TicketSummary.from(ticket),
                ticket.getEscalationReason(),
                ticket.getResolutionNotes(),
                ticket.getFirstResponseAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                messages.findByTicketOrderByCreatedAtAsc(ticket).stream().map(MessageView::from).toList()
        );
    }

    private String titleFrom(String message) {
        String clean = message.trim().replaceAll("\\s+", " ");
        return clean.length() <= 70 ? clean : clean.substring(0, 67) + "...";
    }

    private TicketPriority higherPriority(TicketPriority current, TicketPriority proposed) {
        return current.ordinal() >= proposed.ordinal() ? current : proposed;
    }
}
