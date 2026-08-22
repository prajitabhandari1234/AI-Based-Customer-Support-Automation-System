package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.service.NotificationService;

/**
 * REST controller responsible for handling HTTP requests related to
 * {@link Notification} entities.
 *
 * <p>
 * Provides API endpoints for creating, retrieving, updating,
 * and deleting notifications through the {@link NotificationService}.
 * </p>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Constructs a new {@code NotificationController} with the required
     * notification service.
     *
     * @param notificationService service used to manage notification operations
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retrieves all notifications.
     *
     * @return a list of all notifications
     */
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    /**
     * Retrieves a notification by identifier.
     *
     * @param notificationId the identifier of the notification
     * @return the requested notification, or HTTP 404 if not found
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long notificationId) {

        return notificationService.getNotificationById(notificationId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new notification.
     *
     * @param notification the notification to create
     * @return the created notification
     */
    @PostMapping
    public Notification createNotification(
            @RequestBody Notification notification) {

        return notificationService.saveNotification(notification);
    }

    /**
     * Updates an existing notification.
     *
     * @param notificationId the identifier of the notification to update
     * @param notification the updated notification information
     * @return the updated notification, or HTTP 404 if not found
     */
    @PutMapping("/{notificationId}")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable Long notificationId,
            @RequestBody Notification notification) {

        return notificationService.getNotificationById(notificationId)
                .map(existingNotification -> {
                    notification.setNotificationId(notificationId);
                    return ResponseEntity.ok(
                            notificationService.saveNotification(notification));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Deletes a notification by identifier.
     *
     * @param notificationId the identifier of the notification to delete
     * @return HTTP 204 if deleted, or HTTP 404 if not found
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) {

        if (notificationService.getNotificationById(notificationId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}