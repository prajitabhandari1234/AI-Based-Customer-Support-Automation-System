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
 * The logged-in customer identity is used when loading notification data.
 */
@RestController
@RequestMapping("/api/customer/notifications")
public class CustomerNotificationController {

    private final NotificationService notificationService;

    public CustomerNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getCustomerNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getNotificationsForEmail(authentication.getName()));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markNotificationAsRead(
            @PathVariable Long notificationId) {

        return ResponseEntity.ok(
                notificationService.markCurrentUserNotificationAsRead(notificationId));
    }
}
