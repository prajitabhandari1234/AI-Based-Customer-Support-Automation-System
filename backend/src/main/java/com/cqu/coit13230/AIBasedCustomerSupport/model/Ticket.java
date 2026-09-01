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
 * <p>A ticket is created when a customer issue requires formal tracking
 * or human support. Each ticket is associated with the originating
 * conversation and customer and may optionally be assigned to a support
 * agent.</p>
 *
 * <p>The entity also maintains ticket classification, priority, status,
 * AI-generated analysis information, resolution details, and timestamps
 * used to track the ticket lifecycle.</p>
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
     *
     * <p>Each ticket is associated with one conversation, and a
     * conversation can generate at most one support ticket.</p>
     */
    @NotNull(message = "Conversation is required")
    @OneToOne
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private Conversation conversation;

    /**
     * Customer associated with the support ticket.
     *
     * <p>A customer may have multiple support tickets over time.</p>
     */
    @NotNull(message = "Customer is required")
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /**
     * Support agent currently assigned to handle the ticket.
     *
     * <p>This value may be {@code null} while the ticket is waiting
     * to be assigned to a support agent.</p>
     */
    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    /**
     * Category used to classify the type of customer issue.
     */
    @NotNull(message = "Ticket category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    /**
     * Priority level indicating the urgency of the support ticket.
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
     * Sentiment score determined from the customer's interaction.
     *
     * <p>This value may be {@code null} when sentiment analysis has
     * not yet been performed.</p>
     */
    @DecimalMin(
            value = "-1.0",
            message = "Sentiment score must be between -1.0 and 1.0"
    )
    @DecimalMax(
            value = "1.0",
            message = "Sentiment score must be between -1.0 and 1.0"
    )
    private Double sentimentScore;

    /**
     * Confidence score produced by the AI during ticket analysis.
     *
     * <p>This value may be {@code null} when AI analysis has not yet
     * been performed.</p>
     */
    @DecimalMin(
            value = "0.0",
            message = "AI confidence score must be between 0.0 and 1.0"
    )
    @DecimalMax(
            value = "1.0",
            message = "AI confidence score must be between 0.0 and 1.0"
    )
    private Double aiConfidenceScore;

    /**
     * Notes describing the resolution of the customer issue.
     *
     * <p>This value may remain {@code null} until the ticket has
     * been resolved by a support agent.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    /**
     * Date and time when the support ticket was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date and time when the support ticket was most recently updated.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Initializes the creation and modification timestamps before
     * the ticket is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Updates the modification timestamp before changes to the ticket
     * are persisted in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}