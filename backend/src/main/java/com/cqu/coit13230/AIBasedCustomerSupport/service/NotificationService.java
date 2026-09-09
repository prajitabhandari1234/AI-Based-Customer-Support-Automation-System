package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.NotificationRepository;

/**
 * Handles user notifications and read status updates.
 * It also checks notification ownership before changing a read state.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService) {

        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    // Creates a new unread notification and optionally links it to the related ticket.
    public Notification createTicketNotification(User user, Ticket ticket, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTicket(ticket);
        notification.setMessage(message);
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    public Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with ID: " + notificationId));
    }

    public List<Notification> getCurrentUserNotifications() {
        User user = userService.currentUser();
        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    }

    public List<Notification> getNotificationsForEmail(String email) {
        User user = userService.currentUser();

        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenOperationException("You cannot access another user's notifications");
        }

        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());
    }

    // Checks the logged-in user owns the notification before changing its read status.
    public Notification markCurrentUserNotificationAsRead(Long notificationId) {
        User current = userService.currentUser();
        Notification notification = getNotificationById(notificationId);

        if (!notification.getUser().getUserId().equals(current.getUserId())) {
            throw new ForbiddenOperationException("Notification does not belong to the authenticated user");
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId) {
        getNotificationById(notificationId);
        notificationRepository.deleteById(notificationId);
    }
}
