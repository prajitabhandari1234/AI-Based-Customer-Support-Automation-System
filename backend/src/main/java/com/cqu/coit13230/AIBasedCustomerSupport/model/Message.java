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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an individual message exchanged within a customer
 * support conversation.
 *
 * <p>
 * Each message belongs to a conversation and may be sent by a
 * customer, the AI chatbot, or a support agent. The entity also stores
 * optional sentiment information generated during AI processing.
 * </p>
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * Unique identifier for the message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    /**
     * Conversation to which the message belongs.
     *
     * <p>
     * Multiple messages may belong to the same conversation.
     * </p>
     */
    @NotNull(message = "Conversation is required")
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * Registered user who sent the message.
     *
     * <p>
     * This value may be {@code null} when the message is generated
     * by the AI chatbot because the AI is not represented as a user
     * account.
     * </p>
     */
    @ManyToOne
    @JoinColumn(name = "sender_user_id")
    private User senderUser;

    /**
     * Type of sender responsible for the message.
     */
    @NotNull(message = "Sender type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderType senderType;

    /**
     * Text content of the message.
     */
    @NotBlank(message = "Message content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Sentiment score calculated for the message.
     *
     * <p>
     * This value may be {@code null} when sentiment analysis has
     * not yet been performed.
     * </p>
     */
    private Double sentimentScore;

    /**
     * Date and time when the message was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Initializes the message creation timestamp before the entity
     * is persisted for the first time.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}