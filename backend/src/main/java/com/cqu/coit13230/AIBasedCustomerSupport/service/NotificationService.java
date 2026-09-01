package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
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
     * <p>The associated user and ticket are verified before the
     * notification is persisted.</p>
     *
     * @param notification the notification to be saved
     * @return the saved notification
     * @throws ResourceNotFoundException if the user or ticket does not exist
     */
    public Notification saveNotification(Notification notification) {

        Long userId = notification.getUser().getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId));

        Long ticketId = notification.getTicket().getTicketId();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket not found with ID: " + ticketId));

        notification.setUser(user);
        notification.setTicket(ticket);

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