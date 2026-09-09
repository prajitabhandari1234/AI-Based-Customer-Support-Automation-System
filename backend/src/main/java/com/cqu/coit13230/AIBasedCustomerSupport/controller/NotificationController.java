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
 * Handles notification listing, reading and deletion endpoints.
 * It provides the common notification actions used by the frontend notification panel.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(
            NotificationService notificationService,
            UserService userService) {

        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public List<Notification> getNotifications() {
        if (userService.currentUser().getRole() == UserRole.ADMIN) {
            return notificationService.getAllNotifications();
        }
        return notificationService.getCurrentUserNotifications();
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<Notification> getNotificationById(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(notificationService.getNotificationById(notificationId));
    }

    @PostMapping
    public Notification createNotification(
            @Valid @RequestBody Notification notification) {

        return notificationService.saveNotification(notification);
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable Long notificationId,
            @Valid @RequestBody Notification notification) {

        notificationService.getNotificationById(notificationId);
        notification.setNotificationId(notificationId);
        return ResponseEntity.ok(notificationService.saveNotification(notification));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(
                notificationService.markCurrentUserNotificationAsRead(notificationId));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
