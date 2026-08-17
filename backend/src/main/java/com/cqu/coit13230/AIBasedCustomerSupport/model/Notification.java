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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a notification generated within the customer support system.
 *
 * <p>Notifications are used to inform users about important support events,
 * such as ticket escalation, ticket assignment, status changes, or ticket
 * resolution.</p>
 *
 * <p>Each notification is associated with a user and a support ticket and
 * maintains information about whether the notification has been read.</p>
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
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    /**
     * User who receives the notification.
     *
     * <p>The recipient may be a customer or support agent depending on the
     * type of ticket-related event.</p>
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Support ticket associated with the notification.
     */
    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /**
     * Human-readable notification message presented to the user.
     */
    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Indicates whether the notification has been read by the recipient.
     */
    @Column(nullable = false)
    private Boolean isRead = false;

    /**
     * Date and time when the notification was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Initializes the notification creation timestamp before the entity
     * is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (isRead == null) {
            isRead = false;
        }
    }
}