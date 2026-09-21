package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.service.NotificationService;
import com.cqu.coit13230.AIBasedCustomerSupport.service.UserService;

import jakarta.validation.Valid;

/**
 * Handles notification listing, reading, creation, updating, and deletion
 * endpoints.
 *
 * <p>
 * This controller provides the common notification operations used by
 * the frontend notification panel. The notifications returned by the
 * listing endpoint depend on the role of the currently authenticated user.
 * </p>
 *
 * <p>
 * Administrators can retrieve all notifications, while other authenticated
 * users receive notifications associated with their own account.
 * Notification operations are delegated to the {@link NotificationService}
 * and current-user information is obtained through the {@link UserService}.
 * </p>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    private final UserService userService;

    /**
     * Creates the notification controller with the required notification
     * and user services.
     *
     * @param notificationService service responsible for managing
     *                            notification records
     * @param userService         service responsible for retrieving information
     *                            about the currently authenticated user
     */
    public NotificationController(

            NotificationService notificationService,

            UserService userService) {

        this.notificationService = notificationService;

        this.userService = userService;

    }

    /**
     * Retrieves notifications based on the role of the currently
     * authenticated user.
     *
     * <p>
     * If the current user has the {@link UserRole#ADMIN} role, all
     * notifications stored in the system are returned. Otherwise,
     * only notifications associated with the current user are returned.
     * </p>
     *
     * @return a list of notifications available to the current user
     */
    @GetMapping
    public List<Notification> getNotifications() {

        if (userService.currentUser().getRole() == UserRole.ADMIN) {

            return notificationService.getAllNotifications();

        }

        return notificationService.getCurrentUserNotifications();

    }

    /**
     * Retrieves a specific notification using its unique identifier.
     *
     * @param notificationId unique identifier of the notification to retrieve
     * @return a response containing the requested notification
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(

            @PathVariable Long notificationId) {

        return ResponseEntity.ok(notificationService.getNotificationById(notificationId));

    }

    /**
     * Creates and stores a new notification.
     *
     * <p>
     * The supplied notification is validated before being passed to the
     * notification service for persistence.
     * </p>
     *
     * @param notification validated notification information to create
     * @return the newly created and stored notification
     */
    @PostMapping
    public Notification createNotification(

            @Valid @RequestBody Notification notification) {

        return notificationService.saveNotification(notification);

    }

    /**
     * Updates an existing notification identified by its notification ID.
     *
     * <p>
     * The existing notification is first retrieved to confirm that the
     * record exists. The supplied notification is then assigned the same
     * identifier before being saved through the notification service.
     * </p>
     *
     * @param notificationId unique identifier of the notification to update
     * @param notification   validated notification information containing
     *                       the updated values
     * @return a response containing the updated notification
     */
    @PutMapping("/{notificationId}")
    public ResponseEntity<Notification> updateNotification(

            @PathVariable Long notificationId,

            @Valid @RequestBody Notification notification) {

        notificationService.getNotificationById(notificationId);

        notification.setNotificationId(notificationId);

        return ResponseEntity.ok(notificationService.saveNotification(notification));

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
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long notificationId) {

        return ResponseEntity.ok(

                notificationService.markCurrentUserNotificationAsRead(notificationId));

    }

    /**
     * Deletes a notification identified by its unique notification ID.
     *
     * <p>
     * After the notification is successfully deleted, the endpoint returns
     * an HTTP 204 No Content response.
     * </p>
     *
     * @param notificationId unique identifier of the notification to delete
     * @return an empty response indicating successful deletion
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {

        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();

    }

}