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
 * REST controller responsible for customer-specific
 * notification operations.
 *
 * <p>
 * The authenticated customer's identity is obtained from JWT
 * authentication. Customers may retrieve only their own
 * notifications and may mark only their own notifications as read.
 * </p>
 */
@RestController
@RequestMapping("/api/customer/notifications")
public class CustomerNotificationController {

    /**
     * Service used to manage customer notifications.
     */
    private final NotificationService notificationService;

    /**
     * Constructs a new {@code CustomerNotificationController}.
     *
     * @param notificationService service used to manage notifications
     */
    public CustomerNotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    /**
     * Retrieves all notifications belonging to the authenticated
     * customer.
     *
     * <p>
     * Notifications are returned from newest to oldest.
     * </p>
     *
     * @param authentication authenticated customer information
     * @return notifications belonging to the authenticated customer
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getCustomerNotifications(
            Authentication authentication) {

        List<Notification> notifications =
                notificationService.getCustomerNotifications(
                        authentication.getName());

        return ResponseEntity.ok(notifications);
    }

    /**
     * Marks one of the authenticated customer's notifications as read.
     *
     * @param notificationId unique identifier of the notification
     * @param authentication authenticated customer information
     * @return notification after being marked as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        Notification notification =
                notificationService.markCustomerNotificationAsRead(
                        notificationId,
                        authentication.getName());

        return ResponseEntity.ok(notification);
    }
}