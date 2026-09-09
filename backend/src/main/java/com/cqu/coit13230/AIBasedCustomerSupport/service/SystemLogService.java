package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.SystemLogRepository;

/**
 * Stores important application events for system logs.
 * It provides one place for recording system activity in the database.
 */
@Service
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    public SystemLog logEvent(String eventType, String description, User user, Ticket ticket) {
        SystemLog log = new SystemLog();
        log.setEventType(eventType);
        log.setDescription(description);
        log.setUser(user);
        log.setTicket(ticket);
        return systemLogRepository.save(log);
    }

    public void logLoginSuccess(User user) {
        logEvent("LOGIN_SUCCESS", "Successful login", user, null);
    }

    public void logLoginFailure(String email, User user) {
        logEvent("LOGIN_FAILURE", "Failed login attempt for " + email, user, null);
    }

    public void logTicketCreated(Ticket ticket) {
        logEvent("TICKET_CREATED", "Ticket created", ticket.getCustomer(), ticket);
    }

    public void logTicketEscalated(Ticket ticket) {
        logEvent("TICKET_ESCALATED", "Ticket escalated to human support", ticket.getCustomer(), ticket);
    }

    public void logAiResponse(Ticket ticket, String description) {
        logEvent("AI_RESPONSE", description, ticket.getCustomer(), ticket);
    }

    public List<SystemLog> getAllSystemLogs() {
        return systemLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public SystemLog getSystemLogById(Long logId) {
        return systemLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("System log not found with ID: " + logId));
    }
}
