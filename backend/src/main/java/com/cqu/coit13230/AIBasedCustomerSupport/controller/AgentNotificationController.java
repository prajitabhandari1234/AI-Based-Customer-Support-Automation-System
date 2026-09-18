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
 * Handles notification endpoints used by support agents.
 *
 * <p>
 * This controller provides REST API operations that allow authenticated
 * support agents to retrieve their notifications and mark individual
 * notifications as read.
 * </p>
 *
 * <p>
 * The logged-in agent identity is used when retrieving notification data
 * so that notifications are associated with the authenticated user.
 * </p>
 */
@RestController
@RequestMapping("/api/agent/notifications")
public class AgentNotificationController {

    private final NotificationService notificationService;

    /**
     * Creates the agent notification controller with the required
     * notification service.
     *
     * @param notificationService service responsible for retrieving
     *                            and updating notification information
     */
    public AgentNotificationController(NotificationService notificationService) {

        this.notificationService = notificationService;

    }

    /**
     * Retrieves notifications for the currently authenticated support agent.
     *
     * <p>
     * The authenticated user's name, which represents the logged-in user's
     * identity, is passed to the notification service to retrieve the
     * associated notifications.
     * </p>
     *
     * @param authentication authentication information for the currently
     *                       logged-in support agent
     * @return a response containing the list of notifications associated
     *         with the authenticated support agent
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAgentNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getNotificationsForEmail(authentication.getName()));

    }

    /**
     * Marks a specific notification belonging to the current user as read.
     *
     * <p>
     * The notification identifier is passed to the notification service,
     * which performs the operation for the currently authenticated user.
     * </p>
     *
     * @param notificationId unique identifier of the notification to mark as read
     * @return a response containing the updated notification
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.markCurrentUserNotificationAsRead(notificationId));

    }

}