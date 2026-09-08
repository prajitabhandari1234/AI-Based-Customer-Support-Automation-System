package com.cqu.coit13230.AIBasedCustomerSupport.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

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
 * Stores the main details and AI results for a support ticket.
 * The model is persisted with JPA and is used by the related service and repository classes.
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @OneToOne(optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private Conversation conversation;

    @NotNull(message = "Customer is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;

    @Column(length = 180)
    private String title;

    @NotNull(message = "Ticket category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketCategory category = TicketCategory.GENERAL_INQUIRY;

    @NotNull(message = "Ticket priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @NotNull(message = "Ticket status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sentiment sentiment = Sentiment.NEUTRAL;

    @DecimalMin("-1.0")
    @DecimalMax("1.0")
    private Double sentimentScore;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private Double aiConfidenceScore;

    @Column(columnDefinition = "TEXT")
    private String escalationReason;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime escalatedAt;
    private LocalDateTime firstResponseAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    @JsonProperty("id")
    public Long getIdAlias() {
        return ticketId;
    }

    @JsonProperty("escalated")
    public boolean isEscalated() {
        return escalatedAt != null || status == TicketStatus.ESCALATED;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
