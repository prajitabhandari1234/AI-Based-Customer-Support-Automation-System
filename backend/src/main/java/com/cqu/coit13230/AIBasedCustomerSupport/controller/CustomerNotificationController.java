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
 * Handles notification endpoints used by customers.
 *
 * <p>
 * This controller provides REST API operations that allow authenticated
 * customers to retrieve their notifications and mark individual
 * notifications as read.
 * </p>
 *
 * <p>
 * The identity of the logged-in customer is used when retrieving
 * notification data so that the notifications associated with the
 * authenticated customer can be returned.
 * </p>
 */
@RestController
@RequestMapping("/api/customer/notifications")
public class CustomerNotificationController {

    private final NotificationService notificationService;

    /**
     * Creates the customer notification controller with the required
     * notification service.
     *
     * @param notificationService service responsible for retrieving
     *                            and updating customer notifications
     */
    public CustomerNotificationController(NotificationService notificationService) {

        this.notificationService = notificationService;

    }

    /**
     * Retrieves notifications for the currently authenticated customer.
     *
     * <p>
     * The authenticated customer's identity is obtained from the
     * {@link Authentication} object and passed to the notification service
     * to retrieve the notifications associated with that customer.
     * </p>
     *
     * @param authentication authentication information for the currently
     *                       logged-in customer
     * @return a response containing the list of notifications associated
     *         with the authenticated customer
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getCustomerNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getNotificationsForEmail(authentication.getName()));

    }

    /**
     * Marks a specific notification belonging to the current customer as read.
     *
     * <p>
     * The notification identifier is passed to the notification service,
     * which marks the notification associated with the current user as read.
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