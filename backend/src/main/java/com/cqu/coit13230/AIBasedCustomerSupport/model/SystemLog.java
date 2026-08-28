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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a system activity log within the
 * AI-Based Customer Support Automation System.
 *
 * <p>System logs are used to record important application events such as
 * authentication activity, ticket creation, ticket escalation, ticket status
 * changes, and relevant AI processing events.</p>
 *
 * <p>A log entry may optionally be associated with a user and a support
 * ticket depending on the type of event being recorded.</p>
 */
@Entity
@Table(name = "system_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {

    /**
     * Unique identifier for the system log entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    /**
     * User associated with the recorded event.
     *
     * <p>This value may be {@code null} when the event is not associated
     * with a specific system user.</p>
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Support ticket associated with the recorded event.
     *
     * <p>This value may be {@code null} when the event is unrelated to a
     * support ticket.</p>
     */
    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    /**
     * Type of event recorded in the system log.
     *
     * <p>Examples include login attempts, ticket creation, escalation,
     * ticket updates, and AI-related processing events.</p>
     */
    @NotBlank(message = "Event type is required")
    @Column(nullable = false, length = 50)
    private String eventType;

    /**
     * Detailed description of the recorded system event.
     */
    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Date and time when the system event was recorded.
     *
     * <p>This value is generated automatically when the log entry is
     * persisted and cannot be updated afterwards.</p>
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Initializes the creation timestamp before the system log entry
     * is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}