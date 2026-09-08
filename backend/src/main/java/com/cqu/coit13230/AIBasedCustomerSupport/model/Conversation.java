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
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a customer support conversation.
 * The model is persisted with JPA and is used by the related service and repository classes.
 */
@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;

    @NotNull(message = "Customer is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @NotNull(message = "Conversation status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
