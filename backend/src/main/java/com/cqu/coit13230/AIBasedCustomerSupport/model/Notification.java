package com.cqu.coit13230.AIBasedCustomerSupport.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a notification generated within the
 * AI-Based Customer Support Automation System.
 *
 * <p>Notifications are used to inform users about important support events,
 * including ticket escalation, assignment, status changes, and resolution.</p>
 *
 * <p>Each notification is associated with a recipient and a support ticket.
 * The entity also stores the notification message, read status, and
 * creation timestamp.</p>
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    /**
     * Unique identifier for the notification.
     *
     * <p>The identifier is automatically generated when the notification
     * is persisted in the database.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    /**
     * User who receives the notification.
     *
     * <p>The recipient may be a customer or support agent depending on
     * the type of ticket-related event that generated the notification.</p>
     */
    @NotNull(message = "User is required")
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Support ticket associated with the notification.
     *
     * <p>This relationship identifies the ticket responsible for
     * generating the notification.</p>
     */
    @NotNull(message = "Ticket is required")
    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /**
     * Human-readable message presented to the notification recipient.
     *
     * <p>The message describes the ticket-related event or update
     * that the user should be informed about.</p>
     */
    @NotBlank(message = "Notification message is required")
    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Indicates whether the notification has been read by the recipient.
     *
     * <p>The value defaults to {@code false} when the notification is
     * created and can be changed to {@code true} after the recipient
     * has read the notification.</p>
     */
    @Column(nullable = false)
    private Boolean isRead = false;

    /**
     * Date and time when the notification was created.
     *
     * <p>This value is automatically assigned when the notification
     * is first persisted and cannot be updated afterwards.</p>
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Initializes automatically managed fields before the notification
     * is persisted for the first time.
     *
     * <p>The creation timestamp is set to the current date and time.
     * If the read status has not been specified, it defaults to
     * {@code false}.</p>
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (isRead == null) {
            isRead = false;
        }
    }
}