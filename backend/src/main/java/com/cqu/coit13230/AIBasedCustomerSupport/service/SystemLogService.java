package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;

import com.cqu.coit13230.AIBasedCustomerSupport.model.User;

import com.cqu.coit13230.AIBasedCustomerSupport.repository.SystemLogRepository;

/**
 * Provides business logic and persistence operations for application
 * system logs.
 *
 * <p>
 * This service records important system activities such as successful
 * and failed login attempts, ticket creation, ticket escalation, and
 * AI-generated responses. It also provides operations for retrieving
 * stored system log records.
 * </p>
 */
@Service

public class SystemLogService {

    /**
     * Repository used to persist and retrieve system log records.
     */
    private final SystemLogRepository systemLogRepository;

    /**
     * Creates a system log service with the repository required for
     * storing and retrieving application events.
     *
     * @param systemLogRepository repository used to access system log records
     */
    public SystemLogService(SystemLogRepository systemLogRepository) {

        this.systemLogRepository = systemLogRepository;

    }

    /**
     * Creates and persists a system log entry for an application event.
     *
     * <p>
     * The log entry records the event type, event description, associated
     * user, and related ticket when those values are available.
     * </p>
     *
     * @param eventType   type of application event being recorded
     * @param description description of the recorded event
     * @param user        user associated with the event
     * @param ticket      ticket associated with the event
     * @return newly created and persisted system log
     */
    public SystemLog logEvent(String eventType, String description, User user, Ticket ticket) {

        SystemLog log = new SystemLog();

        log.setEventType(eventType);

        log.setDescription(description);

        log.setUser(user);

        log.setTicket(ticket);

        return systemLogRepository.save(log);

    }

    /**
     * Records a successful user login event.
     *
     * @param user user who successfully logged in
     */
    public void logLoginSuccess(User user) {

        logEvent("LOGIN_SUCCESS", "Successful login", user, null);

    }

    /**
     * Records a failed login attempt for the supplied email address.
     *
     * @param email email address used during the failed login attempt
     * @param user  user associated with the login attempt, when available
     */
    public void logLoginFailure(String email, User user) {

        logEvent("LOGIN_FAILURE", "Failed login attempt for " + email, user, null);

    }

    /**
     * Records the creation of a new support ticket.
     *
     * <p>
     * The ticket's customer and the created ticket are associated
     * with the system log entry.
     * </p>
     *
     * @param ticket ticket that was created
     */
    public void logTicketCreated(Ticket ticket) {

        logEvent("TICKET_CREATED", "Ticket created", ticket.getCustomer(), ticket);

    }

    /**
     * Records the escalation of a support ticket to human support.
     *
     * <p>
     * The ticket's customer and the escalated ticket are associated
     * with the system log entry.
     * </p>
     *
     * @param ticket ticket that was escalated
     */
    public void logTicketEscalated(Ticket ticket) {

        logEvent("TICKET_ESCALATED", "Ticket escalated to human support", ticket.getCustomer(), ticket);

    }

    /**
     * Records an AI response associated with a support ticket.
     *
     * @param ticket      ticket associated with the AI response
     * @param description description of the AI response event
     */
    public void logAiResponse(Ticket ticket, String description) {

        logEvent("AI_RESPONSE", description, ticket.getCustomer(), ticket);

    }

    /**
     * Retrieves all stored system logs ordered by creation time
     * according to the repository query.
     *
     * @return list containing all system log records
     */
    public List<SystemLog> getAllSystemLogs() {

        return systemLogRepository.findAllByOrderByCreatedAtDesc();

    }

    /**
     * Retrieves a system log using its unique identifier.
     *
     * @param logId unique identifier of the system log
     * @return system log matching the supplied identifier
     * @throws ResourceNotFoundException if no system log exists with
     *                                   the supplied identifier
     */
    public SystemLog getSystemLogById(Long logId) {

        return systemLogRepository.findById(logId)

                .orElseThrow(() -> new ResourceNotFoundException("System log not found with ID: " + logId));

    }

}