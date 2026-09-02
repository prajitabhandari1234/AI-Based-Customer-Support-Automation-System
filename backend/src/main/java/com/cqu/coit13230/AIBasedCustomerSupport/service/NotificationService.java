package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.exception.ForbiddenOperationException;
import com.cqu.coit13230.AIBasedCustomerSupport.exception.ResourceNotFoundException;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Notification;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.User;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.NotificationRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.UserRepository;

/**
 * Service class responsible for managing {@link Notification} entities.
 *
 * <p>
 * Provides business-layer operations for creating, retrieving,
 * updating, and deleting notifications through the
 * {@link NotificationRepository}.
 * </p>
 *
 * <p>
 * The service also provides secure customer-specific operations
 * for retrieving notifications and marking notifications as read.
 * Customer identity is obtained from JWT authentication rather
 * than being supplied directly by the client.
 * </p>
 */
@Service
public class NotificationService {

    /**
     * Repository used to access notification records.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Repository used to access user records.
     */
    private final UserRepository userRepository;

    /**
     * Repository used to access support ticket records.
     */
    private final TicketRepository ticketRepository;

    /**
     * Constructs a new {@code NotificationService} with the required
     * repository dependencies.
     *
     * @param notificationRepository repository used to access notification data
     * @param userRepository repository used to access user data
     * @param ticketRepository repository used to access ticket data
     */
    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            TicketRepository ticketRepository) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Creates or updates a notification.
     *
     * <p>
     * The associated user and ticket are verified before the
     * notification is persisted.
     * </p>
     *
     * @param notification notification to be saved
     * @return saved notification
     * @throws ResourceNotFoundException if the user or ticket does not exist
     */
    public Notification saveNotification(
            Notification notification) {

        Long userId = notification
                .getUser()
                .getUserId();

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: "
                                        + userId));

        Long ticketId = notification
                .getTicket()
                .getTicketId();

        Ticket ticket = ticketRepository
                .findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: "
                                        + ticketId));

        notification.setUser(user);
        notification.setTicket(ticket);

        return notificationRepository.save(notification);
    }

    /**
     * Creates an unread notification for a ticket-related event.
     *
     * <p>
     * This method is intended for automatic backend workflows such as
     * ticket assignment, status changes, resolution, and closure.
     * </p>
     *
     * @param user recipient of the notification
     * @param ticket ticket associated with the notification
     * @param message notification message
     * @return newly created notification
     * @throws IllegalArgumentException if the notification message is blank
     */
    public Notification createTicketNotification(
            User user,
            Ticket ticket,
            String message) {

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification message must not be blank");
        }

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setTicket(ticket);
        notification.setMessage(message.trim());
        notification.setIsRead(false);

        return saveNotification(notification);
    }

    /**
     * Retrieves all notifications.
     *
     * @return list containing all notifications
     */
    public List<Notification> getAllNotifications() {

        return notificationRepository.findAll();
    }

    /**
     * Retrieves a notification by its unique identifier.
     *
     * @param notificationId unique identifier of the notification
     * @return optional containing the notification if found
     */
    public Optional<Notification> getNotificationById(
            Long notificationId) {

        return notificationRepository.findById(notificationId);
    }

    /**
     * Retrieves all notifications belonging to the authenticated customer.
     *
     * <p>
     * The customer is identified using the email address obtained
     * from JWT authentication. Notifications are returned from newest
     * to oldest.
     * </p>
     *
     * @param customerEmail email address of the authenticated customer
     * @return notifications belonging to the authenticated customer
     * @throws ResourceNotFoundException if the customer cannot be found
     */
    public List<Notification> getCustomerNotifications(
            String customerEmail) {

        String normalizedEmail = customerEmail
                .trim()
                .toLowerCase();

        User customer = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with email: "
                                        + normalizedEmail));

        return notificationRepository
                .findByUserUserIdOrderByCreatedAtDesc(
                        customer.getUserId());
    }

    /**
     * Marks a notification belonging to the authenticated customer
     * as read.
     *
     * <p>
     * Ownership is verified before the notification is updated.
     * A customer cannot mark another user's notification as read.
     * </p>
     *
     * @param notificationId unique identifier of the notification
     * @param customerEmail email address of the authenticated customer
     * @return updated notification marked as read
     * @throws ResourceNotFoundException if the customer or notification
     *         cannot be found
     * @throws ForbiddenOperationException if the notification does not
     *         belong to the authenticated customer
     */
    public Notification markCustomerNotificationAsRead(
            Long notificationId,
            String customerEmail) {

        String normalizedEmail = customerEmail
                .trim()
                .toLowerCase();

        User customer = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with email: "
                                        + normalizedEmail));

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notification not found with ID: "
                                        + notificationId));

        if (!notification.getUser()
                .getUserId()
                .equals(customer.getUserId())) {

            throw new ForbiddenOperationException(
                    "Notification does not belong to the authenticated customer");
        }

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }

    /**
     * Deletes a notification by its identifier.
     *
     * @param notificationId identifier of the notification to delete
     */
    public void deleteNotification(Long notificationId) {

        notificationRepository.deleteById(notificationId);
    }
}