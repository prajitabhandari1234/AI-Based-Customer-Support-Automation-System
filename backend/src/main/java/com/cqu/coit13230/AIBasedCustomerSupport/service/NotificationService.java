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
 * Provides business logic and persistence operations for user
 * notifications within the AI-based customer support system.
 *
 * <p>
 * This service manages notification creation, retrieval, read-status
 * updates, and deletion. It also verifies notification ownership
 * before allowing authenticated users to access or update their
 * notification information.
 * </p>
 */
@Service
public class NotificationService {

    /**
     * Repository used to perform persistence and retrieval operations
     * for notification records.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Service used to obtain information about the currently
     * authenticated user.
     */
    private final UserService userService;

    /**
     * Creates a notification service with the dependencies required
     * for notification persistence and authenticated user access.
     *
     * @param notificationRepository repository used to access notification records
     * @param userService            service used to access the currently
     *                               authenticated user
     */
    public NotificationService(
            NotificationRepository notificationRepository,
            UserService userService) {

        this.notificationRepository = notificationRepository;

        this.userService = userService;

    }

    /**
     * Saves the supplied notification to the database.
     *
     * @param notification notification to persist
     * @return persisted notification
     */
    public Notification saveNotification(Notification notification) {

        return notificationRepository.save(notification);

    }

    /**
     * Creates and persists a new unread notification for a user and
     * optionally associates it with a related ticket.
     *
     * <p>
     * The notification is assigned to the supplied user, linked to the
     * supplied ticket, populated with the provided message, and initially
     * marked as unread.
     * </p>
     *
     * @param user    user who will receive the notification
     * @param ticket  ticket associated with the notification
     * @param message notification message presented to the user
     * @return newly created and persisted notification
     */
    public Notification createTicketNotification(User user, Ticket ticket, String message) {

        Notification notification = new Notification();

        notification.setUser(user);

        notification.setTicket(ticket);

        notification.setMessage(message);

        notification.setIsRead(false);

        return notificationRepository.save(notification);

    }

    /**
     * Retrieves all notifications stored in the database.
     *
     * @return list containing all stored notifications
     */
    public List<Notification> getAllNotifications() {

        return notificationRepository.findAll();

    }

    /**
     * Retrieves a notification using its unique identifier.
     *
     * @param notificationId unique identifier of the notification
     * @return notification matching the supplied identifier
     * @throws ResourceNotFoundException if no notification exists with
     *                                   the supplied identifier
     */
    public Notification getNotificationById(Long notificationId) {

        return notificationRepository.findById(notificationId)

                .orElseThrow(() -> new ResourceNotFoundException(

                        "Notification not found with ID: " + notificationId));

    }

    /**
     * Retrieves notifications belonging to the currently authenticated user.
     *
     * <p>
     * Notifications are retrieved using the authenticated user's identifier
     * and are returned according to the ordering defined by the repository.
     * </p>
     *
     * @return list of notifications belonging to the currently authenticated user
     */
    public List<Notification> getCurrentUserNotifications() {

        User user = userService.currentUser();

        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());

    }

    /**
     * Retrieves notifications for the supplied email address after
     * verifying that it belongs to the currently authenticated user.
     *
     * <p>
     * If the supplied email address does not match the authenticated
     * user's email address, access to the notifications is rejected.
     * </p>
     *
     * @param email email address whose notifications are requested
     * @return list of notifications belonging to the authenticated user
     * @throws ForbiddenOperationException if the supplied email address
     *                                     belongs to another user
     */
    public List<Notification> getNotificationsForEmail(String email) {

        User user = userService.currentUser();

        if (!user.getEmail().equalsIgnoreCase(email)) {

            throw new ForbiddenOperationException("You cannot access another user's notifications");

        }

        return notificationRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId());

    }

    /**
     * Marks a notification belonging to the currently authenticated
     * user as read.
     *
     * <p>
     * The notification is retrieved first and its owner identifier is
     * compared with the identifier of the authenticated user. The read
     * status is changed only when the notification belongs to that user.
     * </p>
     *
     * @param notificationId unique identifier of the notification to mark as read
     * @return updated and persisted notification
     * @throws ResourceNotFoundException   if no notification exists with
     *                                     the supplied identifier
     * @throws ForbiddenOperationException if the notification does not
     *                                     belong to the authenticated user
     */
    public Notification markCurrentUserNotificationAsRead(Long notificationId) {

        User current = userService.currentUser();

        Notification notification = getNotificationById(notificationId);

        if (!notification.getUser().getUserId().equals(current.getUserId())) {

            throw new ForbiddenOperationException("Notification does not belong to the authenticated user");

        }

        notification.setIsRead(true);

        return notificationRepository.save(notification);

    }

    /**
     * Deletes a notification using its unique identifier.
     *
     * <p>
     * The notification is retrieved first to verify that it exists
     * before the repository deletion operation is performed.
     * </p>
     *
     * @param notificationId unique identifier of the notification to delete
     * @throws ResourceNotFoundException if no notification exists with
     *                                   the supplied identifier
     */
    public void deleteNotification(Long notificationId) {

        getNotificationById(notificationId);

        notificationRepository.deleteById(notificationId);

    }

}