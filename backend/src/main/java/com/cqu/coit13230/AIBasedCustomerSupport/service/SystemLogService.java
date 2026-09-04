package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.SystemLog;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.SystemLogRepository;

/**
 * Service class responsible for managing system activity logs.
 *
 * <p>
 * The service records important application events including
 * authentication attempts, ticket creation, ticket escalation,
 * and AI-generated responses.
 * </p>
 *
 * <p>
 * It also provides administrator operations for retrieving
 * and managing stored system log records.
 * </p>
 */
@Service
public class SystemLogService {

    private static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    private static final String LOGIN_FAILURE = "LOGIN_FAILURE";
    private static final String TICKET_CREATED = "TICKET_CREATED";
    private static final String TICKET_ESCALATED = "TICKET_ESCALATED";
    private static final String AI_RESPONSE_GENERATED =
            "AI_RESPONSE_GENERATED";

    private final SystemLogRepository systemLogRepository;

    /**
     * Constructs the system log service.
     *
     * @param systemLogRepository repository used to access system log data
     */
    public SystemLogService(
            SystemLogRepository systemLogRepository) {

        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Creates or updates a system log record.
     *
     * @param systemLog system log record to save
     * @return saved system log record
     */
    public SystemLog saveSystemLog(SystemLog systemLog) {

        return systemLogRepository.save(systemLog);
    }

    /**
     * Records a general system event.
     *
     * @param eventType type of event being recorded
     * @param description description of the event
     * @param user user associated with the event, if available
     * @param ticket ticket associated with the event, if available
     * @return saved system log record
     */
    public SystemLog logEvent(
            String eventType,
            String description,
            User user,
            Ticket ticket) {

        SystemLog systemLog = new SystemLog();

        systemLog.setEventType(eventType);
        systemLog.setDescription(description);
        systemLog.setUser(user);
        systemLog.setTicket(ticket);

        return systemLogRepository.save(systemLog);
    }

    /**
     * Records a successful user login.
     *
     * @param user authenticated user
     * @return saved system log record
     */
    public SystemLog logLoginSuccess(User user) {

        String description =
                "Successful login for user: "
                        + user.getEmail();

        return logEvent(
                LOGIN_SUCCESS,
                description,
                user,
                null);
    }

    /**
     * Records an unsuccessful login attempt.
     *
     * <p>
     * The supplied email address is recorded for auditing purposes,
     * but passwords and other authentication credentials are never
     * stored in the system log.
     * </p>
     *
     * @param email email address used in the failed login attempt
     * @param user matching user if one exists, otherwise {@code null}
     * @return saved system log record
     */
    public SystemLog logLoginFailure(
            String email,
            User user) {

        String safeEmail =
                email == null || email.isBlank()
                        ? "unknown"
                        : email.trim().toLowerCase();

        String description =
                "Failed login attempt for email: "
                        + safeEmail;

        return logEvent(
                LOGIN_FAILURE,
                description,
                user,
                null);
    }

    /**
     * Records the creation of a support ticket.
     *
     * @param ticket newly created support ticket
     * @return saved system log record
     */
    public SystemLog logTicketCreated(Ticket ticket) {

        String description =
                "Support ticket #"
                        + ticket.getTicketId()
                        + " was created.";

        return logEvent(
                TICKET_CREATED,
                description,
                ticket.getCustomer(),
                ticket);
    }

    /**
     * Records the escalation of a support ticket for
     * human assistance.
     *
     * @param ticket escalated support ticket
     * @return saved system log record
     */
    public SystemLog logTicketEscalated(Ticket ticket) {

        String description =
                "Support ticket #"
                        + ticket.getTicketId()
                        + " was escalated for human assistance.";

        return logEvent(
                TICKET_ESCALATED,
                description,
                ticket.getCustomer(),
                ticket);
    }

    /**
     * Records an AI-generated response associated with
     * a support ticket.
     *
     * <p>
     * The actual AI response content is intentionally not stored
     * in the system log. The response itself should remain in the
     * appropriate conversation or message record.
     * </p>
     *
     * @param user user associated with the AI interaction
     * @param ticket ticket associated with the AI interaction,
     *               if available
     * @return saved system log record
     */
    public SystemLog logAiResponse(
            User user,
            Ticket ticket) {

        String description;

        if (ticket != null) {
            description =
                    "AI response generated for support ticket #"
                            + ticket.getTicketId()
                            + ".";
        } else {
            description =
                    "AI response generated for customer interaction.";
        }

        return logEvent(
                AI_RESPONSE_GENERATED,
                description,
                user,
                ticket);
    }

    /**
     * Retrieves all system log records.
     *
     * @return list of all system log records
     */
    public List<SystemLog> getAllSystemLogs() {

        return systemLogRepository.findAll();
    }

    /**
     * Retrieves a system log record by identifier.
     *
     * @param systemLogId identifier of the system log record
     * @return optional containing the system log if found
     */
    public Optional<SystemLog> getSystemLogById(
            Long systemLogId) {

        return systemLogRepository.findById(systemLogId);
    }

    /**
     * Deletes a system log record by identifier.
     *
     * @param systemLogId identifier of the system log record to delete
     */
    public void deleteSystemLog(Long systemLogId) {

        systemLogRepository.deleteById(systemLogId);
    }
}