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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a message sent in a support conversation.
 * The model is persisted with JPA and is used by the related service and repository classes.
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @NotNull(message = "Conversation is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_user_id")
    private User senderUser;

    @NotNull(message = "Sender type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SenderType senderType;

    @NotBlank(message = "Message content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sentiment sentiment = Sentiment.NEUTRAL;

    private Double sentimentScore;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sentiment == null) {
            sentiment = Sentiment.NEUTRAL;
        }
    }
}
