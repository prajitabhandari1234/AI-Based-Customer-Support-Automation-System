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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents an entry stored in the customer support knowledge base.
 *
 * <p>Knowledge base entries contain reusable question patterns and
 * predefined response templates that can be used by the chatbot to
 * answer common customer enquiries.</p>
 *
 * <p>Each entry also records the user who most recently updated it and
 * the corresponding modification timestamp.</p>
 */
@Entity
@Table(name = "knowledge_base_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseEntry {

    /**
     * Unique identifier for the knowledge base entry.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kbId;

    /**
     * Question pattern used to identify matching customer enquiries.
     */
    @Column(nullable = false, length = 500)
    private String questionPattern;

    /**
     * Predefined response template associated with the question pattern.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerTemplate;

    /**
     * Category used to organize the knowledge base entry.
     */
    @Column(nullable = false, length = 50)
    private String category;

    /**
     * User who most recently created or updated the knowledge base entry.
     *
     * <p>This relationship is intended for authorized support agents
     * or administrators responsible for maintaining the knowledge base.</p>
     */
    @ManyToOne
    @JoinColumn(name = "last_updated_by", nullable = false)
    private User lastUpdatedBy;

    /**
     * Date and time when the knowledge base entry was most recently updated.
     */
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    /**
     * Initializes the modification timestamp before the entity is first
     * persisted in the database.
     */
    @PrePersist
    protected void onCreate() {
        lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Updates the modification timestamp before changes to the entity
     * are persisted in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        lastUpdatedAt = LocalDateTime.now();
    }
}