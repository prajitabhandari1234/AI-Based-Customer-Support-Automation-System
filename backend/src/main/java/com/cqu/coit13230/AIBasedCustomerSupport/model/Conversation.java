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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a customer support conversation within the
 * AI-Based Customer Support Automation System.
 *
 * <p>Each conversation belongs to a customer and stores its current
 * status together with the time the conversation started and ended.
 * A customer may have multiple conversations over time.</p>
 */
@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    /** Unique identifier for the conversation. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;

    /**
     * Customer who initiated the conversation.
     *
     * <p>Multiple conversations may belong to the same customer.</p>
     */
    @NotNull(message = "Customer is required")
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /** Current status of the conversation. */
    @NotNull(message = "Conversation status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    /** Date and time when the conversation started. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    /**
     * Date and time when the conversation ended.
     *
     * <p>This value remains {@code null} while the conversation
     * is still active.</p>
     */
    private LocalDateTime endedAt;

    /**
     * Initializes the conversation start time before the entity
     * is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
}