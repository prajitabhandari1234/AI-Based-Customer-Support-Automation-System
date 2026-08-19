package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.NotificationRepository;

/**
 * Service class responsible for managing {@link Notification} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting notifications through the
 * {@link NotificationRepository}.
 * </p>
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Constructs a new {@code NotificationService} with the required
     * notification repository dependency.
     *
     * @param notificationRepository repository used to access notification data
     */
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates or updates a notification.
     *
     * @param notification the notification to be saved
     * @return the saved notification
     */
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    /**
     * Retrieves all notifications.
     *
     * @return a list of all notifications
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /**
     * Retrieves a notification by identifier.
     *
     * @param notificationId the identifier of the notification
     * @return an optional containing the notification if found
     */
    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }

    /**
     * Deletes a notification by identifier.
     *
     * @param notificationId the identifier of the notification to delete
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}