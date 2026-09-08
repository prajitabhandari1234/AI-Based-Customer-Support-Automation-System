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
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores a question pattern and answer used by the knowledge base.
 * The model is persisted with JPA and is used by the related service and repository classes.
 */
@Entity
@Table(name = "knowledge_base_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kbId;

    @NotBlank(message = "Question pattern is required")
    @Column(nullable = false, length = 500)
    private String questionPattern;

    @NotBlank(message = "Answer template is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answerTemplate;

    @NotBlank(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    @PrePersist
    @PreUpdate
    protected void updateTimestamp() {
        lastUpdatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }
}
