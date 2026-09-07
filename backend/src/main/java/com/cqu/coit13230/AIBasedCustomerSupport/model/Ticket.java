package com.cqu.coit13230.AIBasedCustomerSupport.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a customer support ticket within the
 * AI-Based Customer Support Automation System.
 *
 * <p>
 * A ticket is created when a customer issue requires formal tracking
 * or human support. Each ticket is associated with the originating
 * conversation and customer and may optionally be assigned to a
 * support agent.
 * </p>
 *
 * <p>
 * The entity maintains ticket classification, priority, status,
 * AI-generated analysis information, resolution details, escalation
 * information, and lifecycle timestamps.
 * </p>
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    /**
     * Unique identifier for the support ticket.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    /**
     * Conversation from which the support ticket originated.
     */
    @NotNull(message = "Conversation is required")
    @OneToOne
    @JoinColumn(
            name = "conversation_id",
            nullable = false,
            unique = true)
    private Conversation conversation;

    /**
     * Customer associated with the support ticket.
     */
    @NotNull(message = "Customer is required")
    @ManyToOne
    @JoinColumn(
            name = "customer_id",
            nullable = false)
    private User customer;

    /**
     * Support agent currently assigned to handle the ticket.
     *
     * <p>
     * This value may be {@code null} while the ticket has not
     * yet been assigned.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    /**
     * Category used to classify the customer issue.
     */
    @NotNull(message = "Ticket category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    /**
     * Priority level indicating the urgency of the ticket.
     */
    @NotNull(message = "Ticket priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    /**
     * Current lifecycle status of the support ticket.
     */
    @NotNull(message = "Ticket status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    /**
     * Sentiment score determined from the customer interaction.
     *
     * <p>
     * This value may be {@code null} when sentiment analysis has
     * not yet been performed.
     * </p>
     */
    @DecimalMin(
            value = "-1.0",
            message = "Sentiment score must be between -1.0 and 1.0")
    @DecimalMax(
            value = "1.0",
            message = "Sentiment score must be between -1.0 and 1.0")
    private Double sentimentScore;

    /**
     * Confidence score produced by AI during ticket analysis.
     */
    @DecimalMin(
            value = "0.0",
            message = "AI confidence score must be between 0.0 and 1.0")
    @DecimalMax(
            value = "1.0",
            message = "AI confidence score must be between 0.0 and 1.0")
    private Double aiConfidenceScore;

    /**
     * Notes describing the resolution of the customer issue.
     */
    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    /**
     * Date and time when the support ticket was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date and time when the ticket was most recently updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Date and time when the ticket was escalated for human support.
     *
     * <p>
     * This value remains stored even after the ticket moves from
     * ESCALATED to IN_PROGRESS, RESOLVED, or CLOSED. This allows
     * historical escalation analytics to be calculated correctly.
     * </p>
     */
    @Column
    private LocalDateTime escalatedAt;

    /**
     * Date and time when the customer issue was resolved.
     *
     * <p>
     * This timestamp is used together with {@link #createdAt}
     * to calculate ticket resolution duration for analytics.
     * </p>
     */
    @Column
    private LocalDateTime resolvedAt;

    /**
     * Initializes creation and modification timestamps before
     * the ticket is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    /**
     * Updates the modification timestamp before changes to the
     * ticket are persisted.
     */
    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}