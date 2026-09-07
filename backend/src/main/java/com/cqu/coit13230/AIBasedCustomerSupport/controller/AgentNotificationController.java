package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.service.NotificationService;

/**
 * REST controller responsible for support-agent notification operations.
 *
 * <p>
 * Endpoints provided by this controller allow authenticated support
 * agents to retrieve their notifications and mark individual
 * notifications as read.
 * </p>
 *
 * <p>
 * The identity of the support agent is obtained from JWT
 * authentication rather than being supplied directly by the client.
 * This prevents support agents from accessing or modifying
 * notifications belonging to other users.
 * </p>
 */
@RestController
@RequestMapping("/api/agent/notifications")
public class AgentNotificationController {

    /**
     * Service used to manage notification-related operations.
     */
    private final NotificationService notificationService;

    /**
     * Constructs a new {@code AgentNotificationController}.
     *
     * @param notificationService service used to manage notifications
     */
    public AgentNotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    /**
     * Retrieves all notifications belonging to the authenticated
     * support agent.
     *
     * <p>
     * Notifications are returned from newest to oldest and may include
     * events such as newly escalated support tickets that require
     * human assistance.
     * </p>
     *
     * @param authentication authentication information obtained from JWT
     * @return notifications belonging to the authenticated support agent
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAgentNotifications(
            Authentication authentication) {

        List<Notification> notifications =
                notificationService.getAgentNotifications(
                        authentication.getName());

        return ResponseEntity.ok(notifications);
    }

    /**
     * Marks a notification belonging to the authenticated support
     * agent as read.
     *
     * <p>
     * Notification ownership is validated by the service layer before
     * the notification is updated.
     * </p>
     *
     * @param notificationId unique identifier of the notification
     * @param authentication authentication information obtained from JWT
     * @return updated notification marked as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Notification notification =
                notificationService.markAgentNotificationAsRead(
                        notificationId,
                        authentication.getName());

        return ResponseEntity.ok(notification);
    }
}